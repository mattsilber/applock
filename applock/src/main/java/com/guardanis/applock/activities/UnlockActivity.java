package com.guardanis.applock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.views.UnlockViewController;

public class UnlockActivity extends AppCompatActivity implements UnlockViewController.Delegate {

    public static final String INTENT_ALLOW_UNLOCKED_EXIT = "pin__allow_activity_exit"; // false by default

    protected UnlockViewController viewController;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__activity_unlock);

        this.viewController = new UnlockViewController(this, findViewById(R.id.pin__container));
        this.viewController.setDelegate(this);
        this.viewController.setupRootFlow();
    }

    @Override
    public void onUnlockSuccessful() {
        setResult(Activity.RESULT_OK);
        finish();
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
}
