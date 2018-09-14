package com.guardanis.applock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;
import com.guardanis.applock.views.AppLockViewController;
import com.guardanis.applock.views.LockCreationViewController;

public class CreateLockActivity extends AppCompatActivity {

    protected LockCreationViewController viewController;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__lock_creation);

        this.viewController = new LockCreationViewController(findViewById(R.id.pin__container));
        this.viewController.setupCreateFlow(new LockCreationViewController.LockCreationListener() {
            public void onLockSuccessful() {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewController.cancelPendingAuthentications();
    }
}
