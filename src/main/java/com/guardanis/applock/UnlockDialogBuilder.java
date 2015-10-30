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

public class UnlockDialogBuilder implements LockingHelper.LockEventListener, DialogInterface.OnCancelListener, PINInputController.InputEventListener {

    public interface UnlockEventListener extends LockingHelper.LockEventListener {
        public void onCanceled();
    }

    private Activity activity;

    protected PINInputController inputController;
    protected TextView descriptionView;

    protected int inputViewsCount = 0;
    protected boolean passwordCharsEnabled = true;

    protected UnlockEventListener eventListener;
    protected ActionLockingHelper lockingHelper;

    protected Dialog dialog;
    protected View parentView;

    public UnlockDialogBuilder(Activity activity, UnlockEventListener eventListener){
        this.activity = activity;
        this.eventListener = eventListener;
        this.lockingHelper = new ActionLockingHelper(activity, this);
        this.inputViewsCount = activity.getResources().getInteger(R.integer.pin__default_input_count);
        this.passwordCharsEnabled = activity.getResources().getBoolean(R.bool.pin__default_item_password_chars_enabled);
    }

    public UnlockDialogBuilder setInputViewsCount(int inputViewsCount) {
        this.inputViewsCount = inputViewsCount;
        return this;
    }

    public UnlockDialogBuilder setPasswordCharsEnabled(boolean passwordCharsEnabled) {
        this.passwordCharsEnabled = passwordCharsEnabled;
        return this;
    }

    public Dialog show(){
        setupInputViews();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(parentView);
        builder.setOnCancelListener(this);

        dialog = builder.show();
        return dialog;
    }

    protected void setupInputViews(){
        this.parentView = activity.getLayoutInflater().inflate(R.layout.activity_app_lock, null, false);

        PINInputView view = (PINInputView) parentView.findViewById(R.id.pin__input_view);
        inputController = new PINInputController(view, this)
                .setInputNumbersCount(inputViewsCount)
                .setPasswordCharactersEnabled(passwordCharsEnabled);

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

        dismissDialog();
    }

    private void dismissDialog(){
        try{
            if(dialog.getWindow() != null)
                dialog.dismiss();
        }
        catch(Throwable e){ e.printStackTrace(); }

        dialog = null;
    }

}
