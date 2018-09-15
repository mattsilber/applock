package com.guardanis.applock.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.View;

import com.guardanis.applock.views.AppLockViewController;

import java.lang.ref.WeakReference;

public abstract class AppLockDialogBuilder<ALVC extends AppLockViewController> extends BroadcastReceiver {

    public static final String ACTION_NOTIFY_PERMISSION_CHANGE = "com.guardanis.applock.permissions_updated";

    protected WeakReference<Activity> activity;
    protected ALVC viewController;

    protected WeakReference<AppCompatDialog> dialog = new WeakReference<AppCompatDialog>(null);
    protected int layoutResId;

    protected IntentFilter permissionChangeIntentFilter;

    public AppLockDialogBuilder(Activity activity, int layoutResId) {
        this.activity = new WeakReference<Activity>(activity);
        this.layoutResId = layoutResId;

        this.permissionChangeIntentFilter = new IntentFilter();
        this.permissionChangeIntentFilter.addAction(ACTION_NOTIFY_PERMISSION_CHANGE);

        activity.registerReceiver(this, permissionChangeIntentFilter);
    }

    public Dialog show() {
        if (viewController != null)
            throw new RuntimeException("You can only call show() once per AppLockDialogBuilder instance.");

        final Activity activity = this.activity.get();

        if(activity == null)
            return null;

        View parent = activity.getLayoutInflater()
                .inflate(layoutResId, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(parent);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                handleCanceled();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                handleDismissed();
            }
        });

        this.viewController = buildViewControllerInstance(parent);
        this.viewController.setupRootFlow();

        AppCompatDialog dialog = builder.show();

        this.dialog = new WeakReference<AppCompatDialog>(dialog);

        return dialog;
    }

    protected abstract ALVC buildViewControllerInstance(View parent);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!permissionChangeIntentFilter.matchAction(intent.getAction()))
            return;

        this.viewController.handleSettingsOrPermissionsReturn();
    }

    protected void handleCanceled() {
        dismissDialog();
    }

    protected void handleDismissed() {
        this.dialog = new WeakReference<AppCompatDialog>(null);

        Activity activity = this.activity.get();

        if (activity != null)
            activity.unregisterReceiver(this);
    }

    protected void dismissDialog(){
        try {
            AppCompatDialog dialog = this.dialog.get();

            if (dialog == null)
                return;

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

        this.dialog = new WeakReference<AppCompatDialog>(null);
    }

    public ALVC getViewController() {
        return viewController;
    }
}
