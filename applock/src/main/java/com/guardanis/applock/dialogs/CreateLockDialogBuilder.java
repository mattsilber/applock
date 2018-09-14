package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.utils.PINUtils;
import com.guardanis.applock.views.LockCreationViewController;

public class CreateLockDialogBuilder extends AppLockDialogBuilder<LockCreationViewController> {

    public interface LockCreationListener extends LockCreationViewController.LockCreationListener {
        public void onLockCanceled();
    }

    protected LockCreationListener eventListener;

    public CreateLockDialogBuilder(Activity activity, LockCreationListener eventListener){
        super(activity, R.layout.applock__lock_creation);

        this.eventListener = eventListener;
    }

    @Override
    public Dialog show() {
        final Dialog dialog = super.show();

        viewController.setupCreateFlow(new LockCreationViewController.LockCreationListener() {
            public void onLockSuccessful() {
                dismissDialog();

                eventListener.onLockSuccessful();
            }
        });

        return dialog;
    }

    @Override
    protected LockCreationViewController buildViewControllerInstance(View parent) {
        return new LockCreationViewController(parent);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);

        if(eventListener != null)
            eventListener.onLockCanceled();
    }
}
