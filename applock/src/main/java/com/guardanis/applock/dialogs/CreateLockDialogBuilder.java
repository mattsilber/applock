package com.guardanis.applock.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;
import com.guardanis.applock.views.LockCreationViewController;

import java.lang.ref.WeakReference;

public class CreateLockDialogBuilder extends AppLockDialogBuilder<LockCreationViewController> implements LockCreationViewController.Delegate {

    protected WeakReference<Runnable> lockCreatedCallback;
    protected WeakReference<Runnable> canceledCallback;

    public CreateLockDialogBuilder(Activity activity) {
        super(activity, R.layout.applock__lock_creation);
    }

    public CreateLockDialogBuilder onLockCreated(Runnable lockCreatedCallback) {
        this.lockCreatedCallback = new WeakReference<Runnable>(lockCreatedCallback);

        return this;
    }

    public CreateLockDialogBuilder onCanceled(Runnable canceledCallback) {
        this.canceledCallback = new WeakReference<Runnable>(canceledCallback);

        return this;
    }

    @Override
    protected LockCreationViewController buildViewControllerInstance(View parent) {
        LockCreationViewController controller = new LockCreationViewController(activity.get(), parent);
        controller.setDelegate(this);

        return controller;
    }

    @Override
    public void onLockCreated() {
        dismissDialog();

        final Runnable lockCreatedCallback = this.lockCreatedCallback.get();

        if(lockCreatedCallback != null)
            lockCreatedCallback.run();
    }

    @Override
    protected void handleCanceled() {
        super.handleCanceled();

        final Runnable canceledCallback = this.canceledCallback.get();

        if(canceledCallback != null)
            canceledCallback.run();
    }
}
