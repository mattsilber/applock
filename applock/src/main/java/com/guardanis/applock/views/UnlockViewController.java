package com.guardanis.applock.views;

import android.app.Activity;
import android.view.View;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.FingerprintUtils;

import java.lang.ref.WeakReference;

public class UnlockViewController extends AppLockViewController {

    public interface Delegate {
        public void onUnlockSuccessful();
        public void onFingerprintPermissionRequired();
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

        AppLock.UnlockDelegate unlockDelegate = new AppLock.UnlockDelegate() {
            public void onUnlockSuccessful() {
                handleUnlockSuccessful();
            }

            public void onFingerprintPermissionRequired() { }

            public void onUnlockError(String message) {
                setDescription(message);
            }
        };

        AppLock.getInstance(activity)
                .attemptUnlock(input, unlockDelegate);
    }

    protected void setupFingerprintUnlock() {
        this.displayVariant = DisplayVariant.FINGERPRINT_AUTHENTICATION;

        hide(pinInputView);
        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_unlock_fingerprint);

        attemptFingerprintAuthentication();
    }

    protected void attemptFingerprintAuthentication() {
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        AppLock.getInstance(activity)
                .attemptFingerprintUnlock(new AppLock.UnlockDelegate() {
                    public void onUnlockSuccessful() {
                        handleUnlockSuccessful();
                    }

                    public void onFingerprintPermissionRequired() {
                        Delegate delegate = UnlockViewController.this.delegate.get();

                        if (delegate != null)
                            delegate.onFingerprintPermissionRequired();
                    }

                    public void onUnlockError(String message) {
                        setDescription(message);
                    }
                });
    }

    protected void handleUnlockSuccessful() {
        Delegate delegate = this.delegate.get();

        if (delegate != null)
            delegate.onUnlockSuccessful();
    }

    @Override
    public void handleActivityPause() {
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        if (displayVariant == DisplayVariant.FINGERPRINT_AUTHENTICATION) {
            setDescription(R.string.pin__description_create_fingerprint_paused);

            AppLock.getInstance(activity)
                    .cancelPendingAuthentications();
        }
    }

    @Override
    public void handleActivityResume() {
        if (displayVariant == DisplayVariant.FINGERPRINT_AUTHENTICATION)
            setupFingerprintUnlock();
    }
}
