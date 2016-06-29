package com.guardanis.applock;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.guardanis.applock.locking.ActivityLockingHelper;
import com.guardanis.applock.pin.PINInputController;

public class CreateLockActivity extends BaseLockActivity {

    private ActivityLockingHelper lockingHelper;
    private int inputViewsCount = 4;

    private String pinFirst;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_app_lock);
        setup();
    }

    @Override
    protected void setup() {
        super.setup();

        lockingHelper = new ActivityLockingHelper(this, null);
        setupCreateCode();
    }

    private void setupCreateCode() {
        descriptionView.setText(String.format(getString(R.string.pin__description_create),
                String.valueOf(inputViewsCount),
                getString(R.string.app_name)));

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(getString(R.string.pin__unlock_error_insufficient_selection));
                else{
                    pinFirst = input;
                    setupConfirmCode();
                }
            }
        });
    }

    private void setupConfirmCode() {
        descriptionView.setText(getString(R.string.pin__description_confirm));

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(getString(R.string.pin__unlock_error_insufficient_selection));
                else if(input.equals(pinFirst)){
                    Toast.makeText(CreateLockActivity.this, String.format(getString(R.string.pin__toast_lock_success), getString(R.string.app_name)), Toast.LENGTH_LONG)
                            .show();

                    lockingHelper.saveLockPIN(input);

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
