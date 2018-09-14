package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.guardanis.applock.AppLock;
import com.guardanis.applock.R;
import com.guardanis.applock.views.AppLockViewController;
import com.guardanis.applock.views.LockCreationViewController;
import com.guardanis.applock.views.UnlockViewController;

import java.lang.ref.WeakReference;

public class UnlockDialogBuilder extends AppLockDialogBuilder<UnlockViewController> implements AppLock.LockEventListener {

    public interface UnlockEventListener extends AppLock.LockEventListener {
        public void onCanceled();
    }

    protected WeakReference<UnlockEventListener> eventListener;

    public UnlockDialogBuilder(Activity activity, UnlockEventListener eventListener){
        super(activity, R.layout.applock__unlock);

        this.eventListener = new WeakReference<UnlockEventListener>(eventListener);
    }

    @Override
    public Dialog show() {
        Dialog dialog = super.show();

        viewController.setupUnlockFlow(this);

        return dialog;
    }

    @Override
    protected UnlockViewController buildViewControllerInstance(View parent) {
        return new UnlockViewController(parent);
    }

    @Override
    public void onUnlockSuccessful() {
        dismissDialog();

        final UnlockEventListener eventListener = this.eventListener.get();

        if(eventListener != null)
            eventListener.onUnlockSuccessful();
    }

    @Override
    public void onUnlockFailed(String reason) {
        // Handled by vc
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);

        final UnlockEventListener eventListener = this.eventListener.get();

        if(eventListener != null)
            eventListener.onCanceled();
    }
}
