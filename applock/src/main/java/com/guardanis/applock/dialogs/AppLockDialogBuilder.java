package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.guardanis.applock.R;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;
import com.guardanis.applock.views.AppLockViewController;

import java.lang.ref.WeakReference;

public abstract class AppLockDialogBuilder<ALVC extends AppLockViewController> implements DialogInterface.OnCancelListener {

    protected WeakReference<Activity> activity;
    protected ALVC viewController;

    protected Dialog dialog;
    protected int layoutResId;

    public AppLockDialogBuilder(Activity activity, int layoutResId) {
        this.activity = new WeakReference<Activity>(activity);
        this.layoutResId = layoutResId;
    }

    public Dialog show() {
        final Activity activity = this.activity.get();

        if(activity == null)
            return null;

        if (viewController != null)
            throw new RuntimeException("You can only call show() once per AppLockDialogBuilder instance.");

        View parent = activity.getLayoutInflater()
                .inflate(layoutResId, null, false);

        this.viewController = buildViewControllerInstance(parent);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(parent);
        builder.setOnCancelListener(this);

        this.dialog = builder.show();

        return dialog;
    }

    protected abstract ALVC buildViewControllerInstance(View parent);

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

    public ALVC getViewController() {
        return viewController;
    }
}
