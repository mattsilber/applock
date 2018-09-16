package com.guardanis.applock.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;

import java.security.KeyStore;
import java.security.UnrecoverableKeyException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class FingerprintLockService extends LockService {

    public interface AuthenticationDelegate {
        public void onResolutionRequired(int errorCode);
        public void onAuthenticationHelp(int code, CharSequence message);
        public void onAuthenticating(CancellationSignal cancellationSignal);
        public void onAuthenticationSuccess();
        public void onAuthenticationFailed(String message);
    }

    private static final String PREF_ENROLLMENT_ALLOWED = "pin__fingerprint_enrollment_allowed";
    private static final String KEYSTORE_NAME  = "AndroidKeyStore";

    protected CancellationSignal fingerprintCancellationSignal;

    public void enroll(Context context, AuthenticationDelegate delegate) {
        authenticate(context, false, delegate);
    }

    public void authenticate(Context context, AuthenticationDelegate delegate) {
        authenticate(context, true, delegate);
    }

    protected void authenticate(Context context, boolean localEnrollmentRequired, AuthenticationDelegate delegate) {
        int errorCode = getRequiredResolutionErrorCode(context, localEnrollmentRequired);

        if (-1 < errorCode) {
            delegate.onResolutionRequired(errorCode);

            return;
        }

        attemptFingerprintManagerAuthentication(context, delegate);
    }

    /**
     * @return the resolvable error code or -1 if there are no issues requiring a resolution
     */
    protected int getRequiredResolutionErrorCode(Context context, boolean localEnrollmentRequired) {
        FingerprintManagerCompat manager = FingerprintManagerCompat.from(context);

        if (localEnrollmentRequired && !isEnrolled(context))
            return AppLock.ERROR_CODE_FINGERPRINTS_NOT_LOCALLY_ENROLLED;

        if (!isHardwarePresent(context))
            return AppLock.ERROR_CODE_FINGERPRINTS_MISSING_HARDWARE;

        if (!manager.hasEnrolledFingerprints())
            return AppLock.ERROR_CODE_FINGERPRINTS_EMPTY;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED)
            return AppLock.ERROR_CODE_FINGERPRINTS_PERMISSION_REQUIRED;

        return -1;
    }

    protected void attemptFingerprintManagerAuthentication(final Context context, final AuthenticationDelegate delegate) {
        this.fingerprintCancellationSignal = new CancellationSignal();

        FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);

                delegate.onResolutionRequired(R.string.applock__fingerprint_error_unknown);
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);

                delegate.onAuthenticationHelp(helpMsgId, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                notifyEnrolled(context);

                delegate.onAuthenticationSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                delegate.onAuthenticationFailed(context.getString(R.string.applock__fingerprint_error_unrecognized));
            }
        };

        delegate.onAuthenticating(fingerprintCancellationSignal);

        try {
            Cipher cipher = generateAuthCipher(context, false, 0);
            FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);

            FingerprintManagerCompat manager = FingerprintManagerCompat.from(context);
            manager.authenticate(cryptoObject, 0, fingerprintCancellationSignal, callback, null);
        }
        catch (Exception e) {
            e.printStackTrace();

            delegate.onResolutionRequired(AppLock.ERROR_CODE_FINGERPRINTS_MISSING_HARDWARE);
        }
    }

    public boolean isHardwarePresent(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;

        return FingerprintManagerCompat.from(context)
                .isHardwareDetected();
    }

    @Override
    public boolean isEnrolled(Context context) {
        return AppLock.getInstance(context)
                .getPreferences()
                .getBoolean(PREF_ENROLLMENT_ALLOWED, false);
    }

    protected void notifyEnrolled(Context context) {
        AppLock.getInstance(context)
                .getPreferences()
                .edit()
                .putBoolean(PREF_ENROLLMENT_ALLOWED, true)
                .commit();
    }

    protected Cipher generateAuthCipher(Context context, boolean forceRegenerate, int attempts) throws Exception {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return null;

        String alias = context.getString(R.string.applock__fingerprint_alias);

        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_NAME);
        keyStore.load(null);

        if (forceRegenerate || !keyStore.containsAlias(alias)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_NAME);
            keyGenerator.init(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();
        }

        String cipherFormat = String.format(
                "%s/%s/%s",
                KeyProperties.KEY_ALGORITHM_AES,
                KeyProperties.BLOCK_MODE_CBC,
                KeyProperties.ENCRYPTION_PADDING_PKCS7);

        try {
            Cipher cipher = Cipher.getInstance(cipherFormat);
            cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(alias, null));

            return cipher;
        }
        catch (KeyPermanentlyInvalidatedException e) {
            e.printStackTrace();

            if (1 < attempts)
                return generateAuthCipher(context, true, attempts + 1);
        }
        catch (UnrecoverableKeyException e) {
            e.printStackTrace();

            if (1 < attempts)
                return generateAuthCipher(context, true, attempts + 1);
        }

        return null;
    }

    @Override
    public void invalidateEnrollments(Context context) {
        AppLock.getInstance(context)
                .getPreferences()
                .edit()
                .putBoolean(PREF_ENROLLMENT_ALLOWED, false)
                .commit();
    }

    @Override
    public void cancelPendingAuthentications(Context context) {
        if (fingerprintCancellationSignal != null) {
            this.fingerprintCancellationSignal.cancel();
            this.fingerprintCancellationSignal = null;
        }
    }
}
