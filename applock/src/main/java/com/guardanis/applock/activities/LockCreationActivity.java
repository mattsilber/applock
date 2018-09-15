package com.guardanis.applock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.guardanis.applock.R;
import com.guardanis.applock.views.LockCreationViewController;

public class LockCreationActivity extends AppCompatActivity implements LockCreationViewController.Delegate {

    protected LockCreationViewController viewController;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__activity_lock_creation);

        this.viewController = new LockCreationViewController(this, findViewById(R.id.pin__container));
        this.viewController.setDelegate(this);
        this.viewController.setupRootFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewController.cancelPendingAuthentications();
    }

    @Override
    public void onLockCreated() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
