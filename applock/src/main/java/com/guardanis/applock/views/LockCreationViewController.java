package com.guardanis.applock.views;

import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;

import java.lang.ref.WeakReference;

public class LockCreationViewController extends AppLockViewController {

    public interface LockCreationListener {
        public void onLockSuccessful();
    }

    protected WeakReference<View> chooserParent;
    protected String pinFirst;

    public LockCreationViewController(View parent) {
        super(parent);

        this.chooserParent = new WeakReference(parent.findViewById(R.id.pin__create_chooser_items));
    }

    public void setupCreateFlow(LockCreationListener listener) {
        setupCreationChooser(listener);
    }

    private void setupCreationChooser(final LockCreationListener listener) {
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
                        setupPINCreation(listener);
                    }
                });

        parent.findViewById(R.id.pin__create_option_fingerprint)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {

                    }
                });
    }

    private void setupPINCreation(final LockCreationListener listener) {
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
                setupPINConfirmation(listener);
            }
        });
    }

    private void setupPINConfirmation(final LockCreationListener listener) {
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
                    displayIndicatorMessage(R.string.pin__unlock_error_match_failed);
                    setupPINCreation(listener);

                    return;
                }

                final View parent = getParent();

                if (parent == null)
                    return;

                PINUtils.savePIN(parent.getContext(), input);

                displayIndicatorMessage(R.string.pin__toast_lock_success);
                listener.onLockSuccessful();
            }
        });
    }

    private void setupFingerprintAuthentication(final LockCreationListener listener) {
        hide(pinInputView);
        hide(chooserParent);

        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_create_pin);
    }
}
