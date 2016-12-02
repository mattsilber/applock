package com.guardanis.applock.locking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.guardanis.applock.UnlockActivity;
import com.guardanis.applock.UnlockDialogBuilder;

public class ActionLockingHelper extends LockingHelper {

    private ActionLockingHelper(Context context){
        this(context, null);
    }

    public ActionLockingHelper(Context context, LockEventListener eventListener) {
        super(context, eventListener);
    }

    @Override
    public boolean isUnlockRequired() {
        return getSavedLockPIN() != null;
    }

    /**
     * Check if an action-based unlock is required and navigates to the UnlockActivity if true.
     * @return true if unlock is required.
     */
    public static boolean unlockIfRequired(Activity activity){
        ActionLockingHelper helper = new ActionLockingHelper(activity);

        if(helper.isUnlockRequired()){
            Intent intent = new Intent(activity, UnlockActivity.class)
                    .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, true);

            activity.startActivityForResult(intent, REQUEST_CODE_UNLOCK);

            return true;
        }
        else return false;
    }

    /**
     * Check if an action-based unlock is required and opens an Unlock Dialog if true.
     * If not required, it will trigger eventListener.onUnlockSuccessful()
     */
    public static void unlockIfRequired(Activity activity, @NonNull UnlockDialogBuilder.UnlockEventListener eventListener){
        ActionLockingHelper helper = new ActionLockingHelper(activity);

        if(helper.isUnlockRequired())
            new UnlockDialogBuilder(activity, eventListener)
                    .show();
        else eventListener.onUnlockSuccessful();
    }

}
