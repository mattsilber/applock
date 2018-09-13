package com.guardanis.applock.utils;

import android.Manifest;
import android.annotation.TargetApi;
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

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FingerprintUtils {

    public interface AuthenticationEventListener {
        public void onHardwareNotPresent();
        public void onPermissionNotGranted();
        public void onNoFingerprints();
        public void onServiceNotAvailable();
        public void onAuthenticating(CancellationSignal cancellationSignal);
        public void onAuthenticationSuccess();
        public void onAuthenticationFailed(String message);
    }

    private static final String PREF_ENROLLMENT_ALLOWED = "pin__fingerprint_enrollment_allowed";
    private static final String KEYSTORE_NAME  = "AndroidKeyStore";

    public static void attemptUnlock(Context context, AuthenticationEventListener eventListener) {
        if (!isHardwarePresent(context)) {
            eventListener.onHardwareNotPresent();
            return;
        }

        FingerprintManagerCompat manager = FingerprintManagerCompat.from(context);

        if (!(isLocallyEnrolled(context) && manager.hasEnrolledFingerprints())) {
            eventListener.onNoFingerprints();
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            eventListener.onPermissionNotGranted();
            return;
        }

        attemptAuthorization(context, manager, eventListener);
    }

    private static void attemptAuthorization(final Context context, FingerprintManagerCompat manager, final AuthenticationEventListener eventListener) {
        FingerprintManagerCompat.AuthenticationCallback callback = new FingerprintManagerCompat.AuthenticationCallback() {
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                super.onAuthenticationError(errMsgId, errString);

                eventListener.onAuthenticationFailed(context.getString(R.string.pin__fingerprint_error_unknown));
            }

            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
            }

            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                eventListener.onAuthenticationSuccess();
            }

            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                eventListener.onAuthenticationFailed(context.getString(R.string.pin__fingerprint_error_unrecognized));
            }
        };

        CancellationSignal cancellationSignal = new CancellationSignal();

        eventListener.onAuthenticating(cancellationSignal);

        try {
            Cipher cipher = generateAuthCipher(context, false, 0);
            FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);

            manager.authenticate(cryptoObject, 0, cancellationSignal, callback, null);
        }
        catch (Exception e) {
            e.printStackTrace();

            eventListener.onServiceNotAvailable();
        }
    }

    public static boolean isHardwarePresent(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return false;

        return FingerprintManagerCompat.from(context)
                .isHardwareDetected();
    }

    public static boolean isLocallyEnrolled(Context context) {
        return AppLock.getInstance(context)
                .getPreferences()
                .getBoolean(PREF_ENROLLMENT_ALLOWED, false);
    }

    public static void setLocalEnrollmentEnabled(Context context) {
        AppLock.getInstance(context)
                .getPreferences()
                .edit()
                .putBoolean(PREF_ENROLLMENT_ALLOWED, true)
                .commit();
    }

    public static void removeAuthentications(Context context) {
        AppLock.getInstance(context)
                .getPreferences()
                .edit()
                .putBoolean(PREF_ENROLLMENT_ALLOWED, false)
                .commit();
    }

    private static Cipher generateAuthCipher(Context context, boolean forceRegenerate, int attempts) throws Exception {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return null;

        String alias = context.getString(R.string.pin__fingerprint_alias);

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

        return null;
    }
}
