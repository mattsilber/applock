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
import com.guardanis.applock.utils.PINUtils;

import java.lang.ref.WeakReference;

public class LockCreationViewController extends AppLockViewController {

    public interface Delegate {
        public void onLockCreated();
    }

    public enum DisplayVariant {
        CHOOSER,
        PIN_CREATION,
        PIN_CONFIRMATION,
        FINGERPRINT_AUTHENTICATION
    }

    protected DisplayVariant displayVariant = DisplayVariant.CHOOSER;

    protected WeakReference<Delegate> delegate;
    protected WeakReference<View> chooserParent;

    protected String pinFirst;

    public LockCreationViewController(Activity activity, View parent) {
        super(activity, parent);

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
        this.displayVariant = DisplayVariant.CHOOSER;

        hide(fingerprintAuthImageView);
        hide(pinInputView);
        hide(actionSettings);

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
        this.displayVariant = DisplayVariant.PIN_CREATION;

        hide(fingerprintAuthImageView);
        hide(chooserParent);
        hide(actionSettings);

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
        this.displayVariant = DisplayVariant.PIN_CONFIRMATION;

        hide(fingerprintAuthImageView);
        hide(chooserParent);
        hide(actionSettings);

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
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        PINUtils.savePIN(activity, input);

        handleLockCreated();
    }

    protected void setupFingerprintAuthentication() {
        this.displayVariant = DisplayVariant.FINGERPRINT_AUTHENTICATION;

        hide(pinInputView);
        hide(chooserParent);
        hide(actionSettings);

        show(fingerprintAuthImageView);

        setDescription(R.string.pin__description_create_fingerprint);

        attemptFingerprintAuthentication();
    }

    private void attemptFingerprintAuthentication() {
        final Activity activity = this.activity.get();

        if (activity == null)
            return;

        AppLock.getInstance(activity)
                .attemptFingerprintUnlock(false, new AppLock.UnlockDelegate() {
                    public void onUnlockSuccessful() {
                        handleLockCreated();
                    }

                    public void onResolutionRequired(int errorCode) {
                        setDescription(getDescriptionResIdForError(errorCode));
                        updateActionSettings(errorCode);
                        handleInitialErrorPrompt(errorCode);
                    }

                    public void onRecoverableUnlockError(String message) {
                        setDescription(message);
                    }
                });
    }

    protected void handleLockCreated() {
        Delegate delegate = this.delegate.get();

        if (delegate != null)
            delegate.onLockCreated();
    }

    @Override
    public void onActivityPaused() {
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
    public void onActivityResumed() {
        final Activity activity = this.activity.get();

        if (activity == null || displayVariant != DisplayVariant.FINGERPRINT_AUTHENTICATION)
            return;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            setDescription(R.string.pin__fingerprint_error_permission_multiple);
            updateActionSettings(AppLock.ERROR_CODE_FINGERPRINTS_PERMISSION_REQUIRED);
            return;
        }

        setupFingerprintAuthentication();
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
