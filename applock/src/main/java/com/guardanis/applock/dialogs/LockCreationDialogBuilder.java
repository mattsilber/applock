package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.view.View;

import com.guardanis.applock.R;
import com.guardanis.applock.views.LockCreationViewController;

import java.lang.ref.WeakReference;

public class LockCreationDialogBuilder extends AppLockDialogBuilder<LockCreationViewController> implements LockCreationViewController.Delegate {

    protected WeakReference<Runnable> lockCreatedCallback;
    protected WeakReference<Runnable> canceledCallback;

    public LockCreationDialogBuilder(Activity activity) {
        super(activity, R.layout.applock__lock_creation);
    }

    public LockCreationDialogBuilder onLockCreated(Runnable lockCreatedCallback) {
        this.lockCreatedCallback = new WeakReference<Runnable>(lockCreatedCallback);

        return this;
    }

    public LockCreationDialogBuilder onCanceled(Runnable canceledCallback) {
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
