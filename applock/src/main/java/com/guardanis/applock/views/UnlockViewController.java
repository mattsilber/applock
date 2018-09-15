package com.guardanis.applock.views;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.FingerprintUtils;

import java.lang.ref.WeakReference;

public class UnlockViewController extends AppLockViewController implements AppLock.UnlockDelegate {

    public interface Delegate {
        public void onUnlockSuccessful();
    }

    public enum DisplayVariant {
        PIN_UNLOCK,
        FINGERPRINT_AUTHENTICATION
    }

    protected DisplayVariant displayVariant = DisplayVariant.PIN_UNLOCK;

    protected WeakReference<Delegate> delegate;

    public UnlockViewController(Activity activity, View parent) {
        super(activity, parent);
    }

    public UnlockViewController setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);

        return this;
    }

    @Override
    public void setupRootFlow() {
        View parent = this.parent.get();

        if (parent == null)
            return;

        if (FingerprintUtils.isLocallyEnrolled(parent.getContext()))
            setupFingerprintUnlock();
        else
            setupPINUnlock();
    }

    protected void setupPINUnlock() {
        this.displayVariant = DisplayVariant.PIN_UNLOCK;

        hide(fingerprintAuthImageView);
        hide(actionSettings);
        show(pinInputView);

        setDescription(R.string.pin__description_unlock_pin);

        pinInputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(!pinInputController.matchesRequiredPINLength(input)) {
                    setDescription(R.string.pin__unlock_error_insufficient_selection);

                    return;
                }

                attemptPINUnlock(input);
            }
        });
    }

    protected void attemptPINUnlock(String input) {
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        AppLock.getInstance(activity)
                .attemptUnlock(input, this);
    }

    protected void setupFingerprintUnlock() {
        this.displayVariant = DisplayVariant.FINGERPRINT_AUTHENTICATION;

        hide(pinInputView);
        hide(actionSettings);
        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_unlock_fingerprint);

        attemptFingerprintAuthentication();
    }

    protected void attemptFingerprintAuthentication() {
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        AppLock.getInstance(activity)
                .attemptFingerprintUnlock(true, this);
    }

    @Override
    public void onUnlockSuccessful() {
        Delegate delegate = this.delegate.get();

        if (delegate != null)
            delegate.onUnlockSuccessful();
    }

    @Override
    public void onResolutionRequired(int errorCode) {
        setDescription(getDescriptionResIdForError(errorCode));
        updateActionSettings(errorCode);
        handleInitialErrorPrompt(errorCode);
    }

    @Override
    public void onRecoverableUnlockError(String message) {
        setDescription(message);
    }

    @Override
    public void onActivityPaused() {
        final Activity activity = this.activity.get();

        if (activity == null || displayVariant != DisplayVariant.FINGERPRINT_AUTHENTICATION)
            return;

        setDescription(R.string.pin__description_create_fingerprint_paused);

        AppLock.getInstance(activity)
                .cancelPendingAuthentications();
    }

    @Override
    public void onActivityResumed() {
        final Activity activity = this.activity.get();

        if (activity == null || displayVariant != DisplayVariant.FINGERPRINT_AUTHENTICATION)
            return;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            setDescription(R.string.pin__fingerprint_error_permission_multiple);
            updateActionSettings(AppLock.ERROR_CODE_FINGERPRINTS_PERMISSION_REQUIRED);
            return;
        }

        setupFingerprintUnlock();
    }

    @Override
    protected void handleActionSettingsClicked(int errorCode) {
        final Activity activity = this.activity.get();
        Intent intent = getSettingsIntent(errorCode);

        if (activity == null || intent == null)
            return;

        activity.startActivity(intent);
    }
}
