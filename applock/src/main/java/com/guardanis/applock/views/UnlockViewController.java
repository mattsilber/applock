package com.guardanis.applock.views;

import android.view.View;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;

import java.lang.ref.WeakReference;

public class UnlockViewController extends AppLockViewController {

    public UnlockViewController(View parent) {
        super(parent);
    }

    public void setupUnlockFlow(AppLock.LockEventListener listener) {
        setupPINUnlok(listener);
    }

    protected void setupPINUnlok(final AppLock.LockEventListener listener) {
        hide(fingerprintAuthImageView);
        show(pinInputView);

        setDescription(R.string.pin__description_unlock);

        pinInputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(!pinInputController.matchesRequiredPINLength(input)) {
                    setDescription(R.string.pin__unlock_error_insufficient_selection);

                    return;
                }

                attemptUnlock(input, new AppLock.LockEventListener() {
                    public void onUnlockSuccessful() {

                        listener.onUnlockSuccessful();
                    }

                    public void onUnlockFailed(String reason) {
                        setDescription(reason);
                        listener.onUnlockFailed(reason);
                    }
                });
            }
        });
    }

    protected void attemptUnlock(String input, AppLock.LockEventListener listener) {
        final View parent = this.parent.get();

        if (parent == null)
            return;

        AppLock.getInstance(parent.getContext())
                .attemptUnlock(input, listener);
    }

    protected void setupFingerprintUnlock(final AppLock.LockEventListener listener) {
        hide(pinInputView);
        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_create_pin);
    }
}
