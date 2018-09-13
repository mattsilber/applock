package com.guardanis.applock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.guardanis.applock.utils.FingerprintUtils;
import com.guardanis.applock.utils.PINUtils;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class AppLock {

    public interface LockEventListener {
        public void onUnlockSuccessful();
        public void onUnlockFailed(String reason);
    }

    private static AppLock instance;
    public static AppLock getInstance(Context context) {
        if (instance == null)
            instance = new AppLock(context);

        return instance;
    }

    public static final int REQUEST_CODE_UNLOCK = 9371;
    public static final int REQUEST_CODE_CREATE_LOCK = 9372;

    private static final String PREFS = "pin__preferences";

    private static final String PREF_UNLOCK_FAILURE_TIME = "pin__unlock_failure_time";
    private static final String PREF_UNLOCK_SUCCESS_TIME = "pin__unlock_success_time";

    protected Context context;

    protected int retryCount = 1;

    protected AppLock(Context context){
        this.context = context.getApplicationContext();
    }

    public static boolean isUnlockMethodPresent(Context context) {
        return PINUtils.isPINPresent(context) || FingerprintUtils.isLocallyEnrolled(context);
    }

    public boolean isUnlockRequired() {
        if (!isUnlockMethodPresent(context))
            return false;

        int minutes = context.getResources()
                .getInteger(R.integer.pin__default_activity_lock_reenable_minutes);

        return isUnlockRequired(TimeUnit.MINUTES.toMillis(minutes));
    }

    public boolean isUnlockRequired(long lastSuccessValidMs) {
        return lastSuccessValidMs < System.currentTimeMillis() - getUnlockSuccessTime();
    }

    public void attemptFingerprintUnlock(final LockEventListener eventListener) {
        // TODO: the obvious
    }

    public void attemptUnlock(String pin, final LockEventListener eventListener) {
        if(isUnlockFailureBlockEnabled()){
            retryCount++;

            if(getFailureDelayMs() < System.currentTimeMillis() - getUnlockFailureBlockStart())
                resetUnlockFailure();
            else{
                String message = String.format(
                        context.getString(R.string.pin__unlock_error_retry_limit_exceeded),
                        formatTimeRemaining());

                if (eventListener != null)
                    eventListener.onUnlockFailed(message);

                return;
            }
        }

        PINUtils.attemptUnlock(context, pin, new PINUtils.MatchEventListener() {
            public void onNoPIN() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_no_matching_pin_found));
            }

            public void onMatchFail() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_match_failed));
            }

            public void onMatchSuccess() {
                onUnlockSuccessful(eventListener);
            }

            private void onUnlockFailed(String message) {
                retryCount++;

                if (eventListener != null)
                    eventListener.onUnlockFailed(message);

                if(context.getResources().getInteger(R.integer.pin__default_max_retry_count) < retryCount)
                    onFailureExceedsLimit();
            }
        });
    }

    public SharedPreferences getPreferences(){
        return context.getSharedPreferences(PREFS, 0);
    }

    protected void onFailureExceedsLimit(){
        getPreferences()
                .edit()
                .putLong(PREF_UNLOCK_FAILURE_TIME, System.currentTimeMillis())
                .commit();
    }

    public boolean isUnlockFailureBlockEnabled() {
        return context.getResources().getInteger(R.integer.pin__default_max_retry_count) < retryCount
                || System.currentTimeMillis() - getUnlockFailureBlockStart() < getFailureDelayMs();
    }

    protected long getUnlockFailureBlockStart() {
        return getPreferences()
                .getLong(PREF_UNLOCK_FAILURE_TIME, 0);
    }

    protected void onUnlockSuccessful(LockEventListener eventListener) {
        getPreferences()
                .edit()
                .putLong(PREF_UNLOCK_SUCCESS_TIME, System.currentTimeMillis())
                .commit();

        resetUnlockFailure();

        if (eventListener != null)
            eventListener.onUnlockSuccessful();
    }

    protected long getUnlockSuccessTime() {
        return getPreferences()
                .getLong(PREF_UNLOCK_SUCCESS_TIME, 0);
    }

    protected void resetUnlockFailure() {
        retryCount = 1;

        getPreferences()
                .edit()
                .putLong(PREF_UNLOCK_FAILURE_TIME, 0)
                .commit();
    }

    protected String formatTimeRemaining() {
        long millis = getFailureDelayMs() - (System.currentTimeMillis() - getUnlockFailureBlockStart());
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

        if(TimeUnit.MILLISECONDS.toMinutes(millis) < 1)
            return String.format("%d seconds", seconds);
        else
            return String.format("%d minutes, %d seconds", TimeUnit.MILLISECONDS.toMinutes(millis), seconds);
    }

    protected long getFailureDelayMs(){
        return TimeUnit.MINUTES.toMillis(context.getResources()
                .getInteger(R.integer.pin__default_failure_retry_delay));
    }

    /**
     * This will remove all PIN and/or fingerprint data
     */
    public void clearData() {
        PINUtils.removePIN(context);
        FingerprintUtils.removeAuthentications(context);
    }

    public static void onActivityResumed(Activity activity) {
        AppLock helper = new AppLock(activity);

        if(helper.isUnlockRequired()){
            Intent intent = new Intent(activity, UnlockActivity.class)
                    .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, false);

            activity.startActivityForResult(intent, REQUEST_CODE_UNLOCK);
        }
    }

    /**
     * Check if an action-based unlock is required and navigates to the UnlockActivity if true.
     * @return true if unlock is required.
     */
    public static boolean unlockIfRequired(Activity activity) {
        AppLock helper = new AppLock(activity);

        if(helper.isUnlockRequired()){
            Intent intent = new Intent(activity, UnlockActivity.class)
                    .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, true);

            activity.startActivityForResult(intent, REQUEST_CODE_UNLOCK);

            return true;
        }
        else
            return false;
    }

    /**
     * Check if an action-based unlock is required and opens an Unlock Dialog if true.
     * If not required, it will trigger eventListener.onUnlockSuccessful()
     */
    public static void unlockIfRequired(Activity activity, @NonNull UnlockDialogBuilder.UnlockEventListener eventListener) {
        AppLock helper = new AppLock(activity);

        if(helper.isUnlockRequired())
            new UnlockDialogBuilder(activity, eventListener)
                    .show();
        else
            eventListener.onUnlockSuccessful();
    }
}
