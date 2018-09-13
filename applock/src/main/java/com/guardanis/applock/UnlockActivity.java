package com.guardanis.applock;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.guardanis.applock.pin.PINInputController;

public class UnlockActivity extends BaseLockActivity implements AppLock.LockEventListener {

    public static final String INTENT_ALLOW_UNLOCKED_EXIT = "pin_allow_activity_exit"; // false by default

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__main);
        setup();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if(!getIntent().getBooleanExtra(INTENT_ALLOW_UNLOCKED_EXIT, false)){
                Toast.makeText(this, getString(R.string.pin__toast_unlock_required), Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void setup() {
        super.setup();

        setupUnlock();
    }

    private void setupUnlock() {
        descriptionView.setText(R.string.pin__description_unlock);

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                AppLock.getInstance(UnlockActivity.this)
                        .attemptUnlock(input, UnlockActivity.this);
            }
        });
    }

    @Override
    public void onUnlockSuccessful() {
        Toast.makeText(UnlockActivity.this, getString(R.string.pin__toast_unlock_success), Toast.LENGTH_LONG)
                .show();

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onUnlockFailed(String reason) {
        descriptionView.setText(reason);
    }
}
