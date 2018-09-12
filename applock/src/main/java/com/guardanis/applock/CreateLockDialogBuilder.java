package com.guardanis.applock;

import android.app.Activity;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.Toast;

import com.guardanis.applock.locking.ActivityLockingHelper;
import com.guardanis.applock.pin.PINInputController;

public class CreateLockDialogBuilder extends AppLockDialogBuilder<ActivityLockingHelper> {

    public interface LockCreationListener {
        public void onLockSuccessful();
        public void onLockCanceled();
    }

    protected LockCreationListener eventListener;

    protected String pinFirst;

    public CreateLockDialogBuilder(Activity activity, LockCreationListener eventListener){
        super(activity);

        this.eventListener = eventListener;
    }

    @Override
    protected ActivityLockingHelper buildLockingHelper() {
        return new ActivityLockingHelper(activity, null);
    }

    @Override
    protected void setupInputViews(){
        super.setupInputViews();

        setupCreateCode();
    }

    private void setupCreateCode(){
        descriptionView = (TextView) parentView.findViewById(R.id.pin__description);
        descriptionView.setText(String.format(activity.getString(R.string.pin__description_create),
                String.valueOf(inputViewsCount),
                activity.getString(R.string.app_name)));

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(activity.getString(R.string.pin__unlock_error_insufficient_selection));
                else{
                    pinFirst = input;
                    setupConfirmCode();
                }
            }
        });
    }

    private void setupConfirmCode() {
        descriptionView.setText(activity.getString(R.string.pin__description_confirm));

        inputController.setInputEventListener(new PINInputController.InputEventListener() {
            public void onInputEntered(String input) {
                if(input.length() < inputViewsCount)
                    descriptionView.setText(activity.getString(R.string.pin__unlock_error_insufficient_selection));
                else if(input.equals(pinFirst)){
                    Toast.makeText(activity, String.format(activity.getString(R.string.pin__toast_lock_success), activity.getString(R.string.app_name)),
                            Toast.LENGTH_LONG).show();

                    lockingHelper.saveLockPIN(input);

                    if(eventListener != null)
                        eventListener.onLockSuccessful();

                    dismissDialog();
                }
                else {
                    Toast.makeText(activity, activity.getString(R.string.pin__unlock_error_match_failed),
                            Toast.LENGTH_LONG).show();

                    setupCreateCode();
                }
            }
        });
    }

    @Override
    public void onInputEntered(String input) {
        // Do nothing, each mode will add itself
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        if(eventListener != null)
            eventListener.onLockCanceled();

        super.onCancel(dialogInterface);
    }

}
