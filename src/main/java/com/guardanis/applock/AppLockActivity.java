package com.guardanis.applock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;

public class AppLockActivity extends AppCompatActivity implements LockingHelper.LockEventListener {

    public static final String INTENT_OVERRIDE_VIEW_COUNT = "intent_input_views_count";
    public static final String INTENT_DISPLAY_CHARACTERS_AS_PASSWORD = "intent_characters_as_password";

    private PINInputController inputController;
    private TextView descriptionView;

    private LockingHelper lockingHelper;
    private int inputViewsCount = 4;

    private String pinFirst;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_app_lock);
        setup();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if(lockingHelper == null || lockingHelper.isUnlockRequired()){
                Toast.makeText(this, getString(R.string.pin__toast_unlock_required), Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setup() {
        lockingHelper = new LockingHelper(this, this);
        inputViewsCount = getIntent().getIntExtra(INTENT_OVERRIDE_VIEW_COUNT, getResources().getInteger(R.integer.pin__default_input_count));

        PINInputView view = (PINInputView) findViewById(R.id.pin__input_view);
        inputController = new PINInputController(view, null)
                .setInputNumbersCount(inputViewsCount)
                .setPasswordCharactersEnabled(getIntent().getBooleanExtra(INTENT_DISPLAY_CHARACTERS_AS_PASSWORD, getResources().getBoolean(R.bool.pin__default_item_password_chars_enabled)));

        descriptionView = (TextView) findViewById(R.id.pin__description);

        if(lockingHelper.isUnlockRequired())
            setupUnlock();
        else setupCreateCode();
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
                    Toast.makeText(AppLockActivity.this, String.format(getString(R.string.pin__toast_lock_success), getString(R.string.app_name)), Toast.LENGTH_LONG).show();
                    lockingHelper.saveLockPIN(input);
                    setupUnlock();
                }
                else {
                    Toast.makeText(AppLockActivity.this, getString(R.string.pin__unlock_error_match_failed), Toast.LENGTH_LONG).show();
                    setupCreateCode();
                }
            }
        });
    }

    private void setupUnlock() {
        descriptionView.setText(String.format(getString(R.string.pin__description_unlock), getString(R.string.app_name)));

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                lockingHelper.attemptUnlock(input);
            }
        });
    }

    @Override
    public void onUnlockSuccessful() {
        Toast.makeText(AppLockActivity.this, String.format(getString(R.string.pin__toast_unlock_success), getString(R.string.app_name)), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onUnlockFailed(String reason) {
        descriptionView.setText(reason);
    }

}
