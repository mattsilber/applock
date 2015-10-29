package com.guardanis.applock;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.guardanis.applock.locking.ActivityLockingHelper;
import com.guardanis.applock.pin.PINInputController;

public class UnlockActivity extends BaseLockActivity implements ActivityLockingHelper.LockEventListener {

    public static final String INTENT_ALLOW_UNLOCKED_EXIT = "pin_allow_activity_exit"; // false by default

    private ActivityLockingHelper lockingHelper;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_app_lock);
        setup();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            if(!getIntent().getBooleanExtra(INTENT_ALLOW_UNLOCKED_EXIT, false)){
                Toast.makeText(this, getString(R.string.pin__toast_unlock_required), Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void setup() {
        super.setup();

        lockingHelper = new ActivityLockingHelper(this, this);
        setupUnlock();
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
        Toast.makeText(UnlockActivity.this, String.format(getString(R.string.pin__toast_unlock_success), getString(R.string.app_name)), Toast.LENGTH_LONG).show();

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onUnlockFailed(String reason) {
        descriptionView.setText(reason);
    }

}
