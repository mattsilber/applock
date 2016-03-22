package com.guardanis.applock;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.guardanis.applock.locking.ActionLockingHelper;
import com.guardanis.applock.locking.LockingHelper;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;

public class UnlockDialogBuilder extends AppLockDialogBuilder<ActionLockingHelper> implements LockingHelper.LockEventListener {

    public interface UnlockEventListener extends LockingHelper.LockEventListener {
        public void onCanceled();
    }

    protected UnlockEventListener eventListener;

    public UnlockDialogBuilder(Activity activity, UnlockEventListener eventListener){
        super(activity);
        this.eventListener = eventListener;
    }

    @Override
    protected ActionLockingHelper buildLockingHelper() {
        return new ActionLockingHelper(activity, this);
    }

    @Override
    protected void setupInputViews(){
        super.setupInputViews();

        descriptionView = (TextView) parentView.findViewById(R.id.pin__description);
        descriptionView.setText(String.format(activity.getString(R.string.pin__description_unlock), activity.getString(R.string.app_name)));
    }

    @Override
    public void onInputEntered(String input) {
        lockingHelper.attemptUnlock(input);
    }

    @Override
    public void onUnlockSuccessful() {
        if(eventListener != null)
            eventListener.onUnlockSuccessful();

        dismissDialog();
    }

    @Override
    public void onUnlockFailed(String reason) {
        descriptionView.setText(reason);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if(eventListener != null)
            eventListener.onCanceled();

        super.onCancel(dialogInterface);
    }

}
