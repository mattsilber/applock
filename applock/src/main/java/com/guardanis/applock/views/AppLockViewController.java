package com.guardanis.applock.views;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.os.CancellationSignal;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.dialogs.AppLockDialogBuilder;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;
import com.guardanis.applock.utils.LifeCycleUtils;

import java.lang.ref.WeakReference;

public abstract class AppLockViewController extends BroadcastReceiver {

    protected PINInputController pinInputController;

    protected WeakReference<Activity> activity;
    protected WeakReference<View> parent;

    protected WeakReference<PINInputView> pinInputView;
    protected WeakReference<AppCompatImageView> fingerprintAuthImageView;

    protected WeakReference<TextView> descriptionView;

    protected CancellationSignal fingerprintAuthCancelSignal;

    protected Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    protected IntentFilter activityLifeCycleIntentFilter = LifeCycleUtils.buildIntentFilter();

    public AppLockViewController(Activity activity, View parent) {
        this.activity = new WeakReference<Activity>(activity);
        this.parent = new WeakReference<View>(parent);
        this.descriptionView = new WeakReference((TextView) parent.findViewById(R.id.pin__description));

        this.pinInputView = new WeakReference((PINInputView) parent.findViewById(R.id.pin__input_view));
        this.fingerprintAuthImageView = new WeakReference(parent.findViewById(R.id.pin__fingerprint_image));

        int inputViewsCount = parent.getResources()
                .getInteger(R.integer.pin__default_input_count);

        boolean passwordCharsEnabled = parent.getResources()
                .getBoolean(R.bool.pin__default_item_password_chars_enabled);

        this.pinInputController = new PINInputController(pinInputView.get())
                .setInputNumbersCount(inputViewsCount)
                .setPasswordCharactersEnabled(passwordCharsEnabled);

        this.activityLifecycleCallbacks = LifeCycleUtils.attach(activity, this, activityLifeCycleIntentFilter);
    }

    public abstract void setupRootFlow();

    public abstract void handleActivityPause();
    public abstract void handleActivityResume();

    public void setDescription(int descriptionResId) {
        final TextView descriptionView = this.descriptionView.get();

        if (descriptionView == null)
            return;

        descriptionView.setText(descriptionResId);
    }

    public void setDescription(String description) {
        final TextView descriptionView = this.descriptionView.get();

        if (descriptionView == null)
            return;

        descriptionView.setText(description);
    }

    protected <T extends View> void hide(WeakReference<T> weakView) {
        final T view = weakView.get();

        if (view == null)
            return;

        view.setVisibility(View.GONE);
    }

    protected <T extends View> void show(WeakReference<T> weakView) {
        final T view = weakView.get();

        if (view == null)
            return;

        view.setVisibility(View.VISIBLE);
    }

    public PINInputController getPINInputController() {
        return pinInputController;
    }

    public View getParent() {
        return parent.get();
    }

    public void cancelPendingAuthentications() {
        if (fingerprintAuthCancelSignal == null)
            return;

        this.fingerprintAuthCancelSignal.cancel();
        this.fingerprintAuthCancelSignal = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!activityLifeCycleIntentFilter.matchAction(intent.getAction()))
            return;

        switch (intent.getAction()) {
            case LifeCycleUtils.ACTION_NOTIFY_ACTIVITY_PAUSE:
                handleActivityPause();
                break;
            case LifeCycleUtils.ACTION_NOTIFY_ACTIVITY_RESUME:
                handleActivityResume();
                break;
            default:
                break;
        }
    }

    public void unregisterReceivers() {
        Activity activity = this.activity.get();

        if (activity == null)
            return;

        activity.unregisterReceiver(this);

        activity.getApplication()
                .unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }
}
