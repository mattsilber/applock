package com.guardanis.applock;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.TextView;

public class UnlockDialogBuilder extends AppLockDialogBuilder implements AppLock.LockEventListener {

    public interface UnlockEventListener extends AppLock.LockEventListener {
        public void onCanceled();
    }

    protected UnlockEventListener eventListener;

    public UnlockDialogBuilder(Activity activity, UnlockEventListener eventListener){
        super(activity);

        this.eventListener = eventListener;
    }

    @Override
    protected void setupInputViews(){
        super.setupInputViews();

        descriptionView = (TextView) parentView.findViewById(R.id.pin__description);
        descriptionView.setText(R.string.pin__description_unlock);
    }

    @Override
    public void onInputEntered(String input) {
        AppLock.getInstance(activity)
                .attemptUnlock(input, this);
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
