package com.guardanis.applock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;

public class CreateLockActivity extends BaseLockActivity {

    private int inputViewsCount = 4;

    private String pinFirst;

    protected View chooserParent;
    protected View fingerprintAuthImageView;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__main);
        setup();
    }

    @Override
    protected void setup() {
        super.setup();

        this.chooserParent = findViewById(R.id.pin__create_chooser_items);
        this.fingerprintAuthImageView = findViewById(R.id.pin__fingerprint_image);

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
                    Toast.makeText(CreateLockActivity.this, getString(R.string.pin__toast_lock_success), Toast.LENGTH_LONG)
                            .show();

                    PINUtils.savePIN(CreateLockActivity.this, input);

                    setResult(Activity.RESULT_OK);
                    finish();
                }
                else {
                    Toast.makeText(CreateLockActivity.this, getString(R.string.pin__unlock_error_match_failed), Toast.LENGTH_LONG)
                            .show();

                    setupCreateCode();
                }
            }
        });
    }
}
