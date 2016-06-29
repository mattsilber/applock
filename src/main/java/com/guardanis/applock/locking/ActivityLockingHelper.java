package com.guardanis.applock.locking;

import android.app.Activity;
import android.content.Intent;

import com.guardanis.applock.R;
import com.guardanis.applock.UnlockActivity;

import java.util.concurrent.TimeUnit;

public class ActivityLockingHelper extends LockingHelper {

    private ActivityLockingHelper(Activity activity){
        this(activity, null);
    }

    public ActivityLockingHelper(Activity activity, LockEventListener eventListener) {
        super(activity, eventListener);
    }

    @Override
    public boolean isUnlockRequired() {
        return getSavedLockPIN() != null
                && TimeUnit.MINUTES.toMillis(activity.getResources().getInteger(R.integer.pin__default_activity_lock_reenable_minutes)) < System.currentTimeMillis() - getUnlockSuccessTime();
    }

    public static void onActivityResumed(Activity activity){
        ActivityLockingHelper helper = new ActivityLockingHelper(activity);

        if(helper.isUnlockRequired()){
            Intent intent = new Intent(activity, UnlockActivity.class)
                    .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, false);

            activity.startActivityForResult(intent, REQUEST_CODE_UNLOCK);
        }
    }

}
