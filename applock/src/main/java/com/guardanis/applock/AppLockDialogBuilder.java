package com.guardanis.applock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;

public abstract class AppLockDialogBuilder implements DialogInterface.OnCancelListener, PINInputController.InputEventListener {

    protected Activity activity;

    protected PINInputController inputController;
    protected PINInputView pinInputView;
    protected TextView descriptionView;

    protected int inputViewsCount = 0;
    protected boolean passwordCharsEnabled = true;

    protected Dialog dialog;
    protected View parentView;

    public AppLockDialogBuilder(Activity activity){
        this.activity = activity;

        this.inputViewsCount = activity.getResources()
                .getInteger(R.integer.pin__default_input_count);

        this.passwordCharsEnabled = activity.getResources()
                .getBoolean(R.bool.pin__default_item_password_chars_enabled);
    }

    public AppLockDialogBuilder setInputViewsCount(int inputViewsCount) {
        this.inputViewsCount = inputViewsCount;
        return this;
    }

    public AppLockDialogBuilder setPasswordCharsEnabled(boolean passwordCharsEnabled) {
        this.passwordCharsEnabled = passwordCharsEnabled;
        return this;
    }

    public Dialog show(){
        setupInputViews();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(parentView);
        builder.setOnCancelListener(this);

        this.dialog = builder.show();

        return dialog;
    }

    protected void setupInputViews(){
        this.parentView = activity.getLayoutInflater()
                .inflate(R.layout.applock__main, null, false);

        this.descriptionView = (TextView) parentView.findViewById(R.id.pin__description);
        this.pinInputView = (PINInputView) parentView.findViewById(R.id.pin__input_view);

        this.inputController = new PINInputController(pinInputView, this)
                .setInputNumbersCount(inputViewsCount)
                .setPasswordCharactersEnabled(passwordCharsEnabled);
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        dismissDialog();
    }

    protected void dismissDialog(){
        try{
            // Attempt to prevent stupid fucking uncatchable system error "View not Attached to Window" when dialog dismissed
            Context context = ((ContextWrapper) dialog.getContext()).getBaseContext();
            if(context instanceof Activity) {
                if(!((Activity) context).isFinishing()){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if(!((Activity) context).isDestroyed())
                            dialog.dismiss();
                    }
                    else
                        dialog.dismiss();
                }
            }
            else
                dialog.dismiss();
        }
        catch(IllegalArgumentException e){ }
        catch(Throwable e){ }

        this.dialog = null;
    }
}
