package com.guardanis.applock;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;

public class CreateLockDialogBuilder extends AppLockDialogBuilder {

    public interface LockCreationListener {
        public void onLockSuccessful();
        public void onLockCanceled();
    }

    protected LockCreationListener eventListener;
    protected String pinFirst;

    protected View chooserParent;
    protected View fingerprintAuthImageView;

    public CreateLockDialogBuilder(Activity activity, LockCreationListener eventListener){
        super(activity);

        this.eventListener = eventListener;
    }

    @Override
    protected void setupInputViews() {
        super.setupInputViews();

        this.chooserParent = parentView.findViewById(R.id.pin__create_chooser_items);
        this.fingerprintAuthImageView = parentView.findViewById(R.id.pin__fingerprint_image);

        setupCreateCode();
    }

    private void setupCreateCode() {
        chooserParent.setVisibility(View.GONE);
        fingerprintAuthImageView.setVisibility(View.GONE);
        pinInputView.setVisibility(View.VISIBLE);

        descriptionView.setText(R.string.pin__description_create_pin);

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(R.string.pin__unlock_error_insufficient_selection);
                else{
                    pinFirst = input;
                    setupConfirmCode();
                }
            }
        });
    }

    private void setupConfirmCode() {
        descriptionView.setText(R.string.pin__description_confirm);

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(R.string.pin__unlock_error_insufficient_selection);
                else if(input.equals(pinFirst)) {
                    Toast.makeText(activity, activity.getString(R.string.pin__toast_lock_success), Toast.LENGTH_LONG)
                            .show();

                    PINUtils.savePIN(activity, input);

                    if(eventListener != null)
                        eventListener.onLockSuccessful();

                    dismissDialog();
                }
                else {
                    Toast.makeText(activity, activity.getString(R.string.pin__unlock_error_match_failed), Toast.LENGTH_LONG)
                            .show();

                    setupCreateCode();
                }
            }
        });
    }

    private void setupChooser() {
        chooserParent.setVisibility(View.VISIBLE);
        fingerprintAuthImageView.setVisibility(View.GONE);
        pinInputView.setVisibility(View.GONE);

        descriptionView.setText(R.string.pin__description_chooser);


    }

    @Override
    public void onInputEntered(String input) {
        // Do nothing, each mode will add itself
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if(eventListener != null)
            eventListener.onLockCanceled();

        super.onCancel(dialogInterface);
    }
}
