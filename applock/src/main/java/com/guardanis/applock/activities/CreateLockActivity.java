package com.guardanis.applock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;
import com.guardanis.applock.views.AppLockViewController;
import com.guardanis.applock.views.LockCreationViewController;

public class CreateLockActivity extends AppCompatActivity implements LockCreationViewController.Delegate {

    protected LockCreationViewController viewController;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setContentView(R.layout.applock__lock_creation);

        this.viewController = new LockCreationViewController(findViewById(R.id.pin__container));
        this.viewController.setDelegate(this);
        this.viewController.setupRootFlow();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        viewController.cancelPendingAuthentications();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // TODO
    }

    @Override
    public void onLockCreated() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onFingerprintPermissionRequired() {
        // TODO
    }
}
