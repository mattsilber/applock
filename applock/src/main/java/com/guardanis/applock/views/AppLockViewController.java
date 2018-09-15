package com.guardanis.applock.views;

import android.app.Dialog;
import android.support.v4.os.CancellationSignal;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guardanis.applock.R;
import com.guardanis.applock.dialogs.AppLockDialogBuilder;
import com.guardanis.applock.pin.PINInputController;
import com.guardanis.applock.pin.PINInputView;

import java.lang.ref.WeakReference;

public abstract class AppLockViewController {

    protected PINInputController pinInputController;

    protected WeakReference<View> parent;

    protected WeakReference<PINInputView> pinInputView;
    protected WeakReference<AppCompatImageView> fingerprintAuthImageView;

    protected WeakReference<TextView> descriptionView;

    protected CancellationSignal fingerprintAuthCancelSignal;

    public AppLockViewController(View parent) {
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
    }

    public abstract void setupRootFlow();

    public abstract void handleSettingsOrPermissionsReturn();

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

    protected void displayIndicatorMessage(int messageResId) {
        final View parent = this.parent.get();

        if (parent == null)
            return;

        Toast.makeText(parent.getContext(), parent.getResources().getString(messageResId), Toast.LENGTH_SHORT)
                .show();
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
}
