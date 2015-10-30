package com.guardanis.applock.locking;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.guardanis.applock.UnlockActivity;
import com.guardanis.applock.UnlockDialogBuilder;

public class ActionLockingHelper extends LockingHelper {

    private ActionLockingHelper(Activity activity){
        this(activity, null);
    }

    public ActionLockingHelper(Activity activity, LockEventListener eventListener) {
        super(activity, eventListener);
    }

    @Override
    public boolean isUnlockRequired() {
        return getSavedLockPIN() != null;
    }

    /**
     * Check if an action-based unlock is required and navigates to the UnlockActivity if true.
     * @return true is unlock is required.
     */
    public static boolean unlockIfRequired(Activity activity){
        ActionLockingHelper helper = new ActionLockingHelper(activity);
        if(helper.isUnlockRequired()){
            Intent intent = new Intent(activity, UnlockActivity.class);
            intent.putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, true);
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
        if(helper.isUnlockRequired()){
            UnlockDialogBuilder builder = new UnlockDialogBuilder(activity, eventListener);
            builder.show();
        }
        else eventListener.onUnlockSuccessful();
    }

}
