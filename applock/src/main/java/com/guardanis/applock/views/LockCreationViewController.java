package com.guardanis.applock.views;

import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;

import java.lang.ref.WeakReference;

public class LockCreationViewController extends AppLockViewController {

    public interface Delegate {
        public void onLockCreated();
        public void onFingerprintPermissionRequired();
    }

    protected WeakReference<Delegate> delegate;
    protected WeakReference<View> chooserParent;
    protected String pinFirst;

    public LockCreationViewController(View parent) {
        super(parent);

        this.chooserParent = new WeakReference(parent.findViewById(R.id.pin__create_chooser_items));
    }

    public LockCreationViewController setDelegate(Delegate delegate) {
        this.delegate = new WeakReference<Delegate>(delegate);

        return this;
    }

    @Override
    public void setupRootFlow() {
        setupCreationChooser();
    }

    protected void setupCreationChooser() {
        hide(fingerprintAuthImageView);
        hide(pinInputView);

        show(chooserParent);

        setDescription(R.string.pin__description_chooser);

        View parent = this.parent.get();

        if (parent == null)
            return;

        parent.findViewById(R.id.pin__create_option_pin)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        setupPINCreation();
                    }
                });

        parent.findViewById(R.id.pin__create_option_fingerprint)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        setupFingerprintAuthentication();
                    }
                });
    }

    protected void setupPINCreation() {
        hide(fingerprintAuthImageView);
        hide(chooserParent);

        show(pinInputView);

        setDescription(R.string.pin__description_create_pin);

        pinInputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(!pinInputController.matchesRequiredPINLength(input)) {
                    setDescription(R.string.pin__unlock_error_insufficient_selection);

                    return;
                }

                pinFirst = input;
                setupPINConfirmation();
            }
        });
    }

    protected void setupPINConfirmation() {
        hide(fingerprintAuthImageView);
        hide(chooserParent);

        show(pinInputView);

        setDescription(R.string.pin__description_confirm);

        pinInputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(!pinInputController.matchesRequiredPINLength(input)) {
                    setDescription(R.string.pin__unlock_error_insufficient_selection);

                    return;
                }

                if(!input.equals(pinFirst)) {
                    setupPINCreation();
                    setDescription(R.string.pin__description_create_pin_reattempt);

                    return;
                }

                createPINLock(input);
            }
        });
    }

    protected void createPINLock(String input) {
        final View parent = getParent();

        if (parent == null)
            return;

        PINUtils.savePIN(parent.getContext(), input);

        displayIndicatorMessage(R.string.pin__toast_lock_success);

        Delegate delegate = this.delegate.get();

        if (delegate != null)
            delegate.onLockCreated();
    }

    protected void setupFingerprintAuthentication() {
        hide(pinInputView);
        hide(chooserParent);

        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_create_fingerprint);

        attemptFingerprintAuthentication();
    }

    private void attemptFingerprintAuthentication() {
        final View parent = getParent();

        if (parent == null)
            return;

        AppLock.getInstance(parent.getContext())
                .attemptFingerprintUnlock(new AppLock.UnlockDelegate() {
                    public void onUnlockSuccessful() {
                        Delegate delegate = LockCreationViewController.this.delegate.get();

                        if (delegate != null)
                            delegate.onLockCreated();
                    }

                    public void onFingerprintPermissionRequired() {
                        Delegate delegate = LockCreationViewController.this.delegate.get();

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
        setupFingerprintAuthentication();
    }
}
