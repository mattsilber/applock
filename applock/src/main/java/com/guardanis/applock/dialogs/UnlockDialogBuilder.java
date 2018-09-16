package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.views.UnlockViewController;

import java.lang.ref.WeakReference;

public class UnlockDialogBuilder extends AppLockDialogBuilder<UnlockViewController> implements UnlockViewController.Delegate {

    protected WeakReference<Runnable> unlockCallback;
    protected WeakReference<Runnable> canceledCallback;

    public UnlockDialogBuilder(Activity activity) {
        super(activity, R.layout.applock__unlock);
    }

    public UnlockDialogBuilder onUnlocked(Runnable unlockCallback) {
        this.unlockCallback = new WeakReference<Runnable>(unlockCallback);

        return this;
    }

    public UnlockDialogBuilder onCanceled(Runnable canceledCallback) {
        this.canceledCallback = new WeakReference<Runnable>(canceledCallback);

        return this;
    }

    @Override
    protected UnlockViewController buildViewControllerInstance(View parent) {
        UnlockViewController controller = new UnlockViewController(activity.get(), parent);
        controller.setDelegate(this);

        return controller;
    }

    @Override
    public void onUnlockSuccessful() {
        dismissDialog();

        final Runnable unlockCallback = this.unlockCallback.get();

        if(unlockCallback != null)
            unlockCallback.run();
    }

    @Override
    protected void handleCanceled() {
        super.handleCanceled();

        final Runnable canceledCallback = this.canceledCallback.get();

        if(canceledCallback != null)
            canceledCallback.run();
    }

    public Dialog showIfEnrolledOrSuccess() {
        Activity activity = this.activity.get();

        if (activity == null)
            return null;

        if (!AppLock.isEnrolled(activity)) {
            final Runnable unlockCallback = this.unlockCallback.get();

            if(unlockCallback != null)
                unlockCallback.run();

            return null;
        }

        return show();
    }

    public Dialog showIfRequiredOrSuccess(long longValidDurationMs) {
        Activity activity = this.activity.get();

        if (activity == null)
            return null;

        if (!AppLock.isUnlockRequired(activity, longValidDurationMs)) {
            final Runnable unlockCallback = this.unlockCallback.get();

            if(unlockCallback != null)
                unlockCallback.run();

            return null;
        }

        return show();
    }
}
