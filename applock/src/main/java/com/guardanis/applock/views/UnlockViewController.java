package com.guardanis.applock.views;

import android.app.Activity;
import android.view.View;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;

import java.lang.ref.WeakReference;

public class UnlockViewController extends AppLockViewController {

    public interface Delegate {
        public void onUnlockSuccessful();
        public void onFingerprintPermissionRequired();
    }

    protected WeakReference<Delegate> delegate;

    public UnlockViewController(View parent) {
        super(parent);
    }

    public UnlockViewController setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);

        return this;
    }

    @Override
    public void setupRootFlow() {
        setupPINUnlock();
    }

    protected void setupPINUnlock() {
        hide(fingerprintAuthImageView);
        show(pinInputView);

        setDescription(R.string.pin__description_unlock);

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
        final View parent = this.parent.get();

        if (parent == null)
            return;

        AppLock.UnlockDelegate unlockDelegate = new AppLock.UnlockDelegate() {
            public void onUnlockSuccessful() {
                Delegate delegate = UnlockViewController.this.delegate.get();

                if (delegate != null)
                    delegate.onUnlockSuccessful();
            }

            public void onFingerprintPermissionRequired() { }

            public void onUnlockError(String message) {
                setDescription(message);
            }
        };

        AppLock.getInstance(parent.getContext())
                .attemptUnlock(input, unlockDelegate);
    }

    protected void setupFingerprintUnlock() {
        hide(pinInputView);
        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_create_pin);

        attemptFingerprintAuthentication();
    }

    protected void attemptFingerprintAuthentication() {
        final View parent = this.parent.get();

        if (parent == null)
            return;

        AppLock.getInstance(parent.getContext())
                .attemptFingerprintUnlock(new AppLock.UnlockDelegate() {
                    public void onUnlockSuccessful() {
                        Delegate delegate = UnlockViewController.this.delegate.get();

                        if (delegate != null)
                            delegate.onUnlockSuccessful();
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

    @Override
    public void handleSettingsOrPermissionsReturn() {
        setupFingerprintUnlock();
    }
}
