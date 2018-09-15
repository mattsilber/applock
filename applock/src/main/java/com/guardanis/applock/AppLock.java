package com.guardanis.applock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.CancellationSignal;

import com.guardanis.applock.activities.UnlockActivity;
import com.guardanis.applock.dialogs.UnlockDialogBuilder;
import com.guardanis.applock.utils.FingerprintUtils;
import com.guardanis.applock.utils.PINUtils;

import java.util.concurrent.TimeUnit;

public class AppLock {

    public interface UnlockDelegate {
        public void onUnlockSuccessful();
        public void onFingerprintPermissionRequired();
        public void onUnlockError(String message);
    }

    private static AppLock instance;
    public static AppLock getInstance(Context context) {
        if (instance == null)
            instance = new AppLock(context);

        return instance;
    }

    public static final int REQUEST_CODE_UNLOCK = 9371;
    public static final int REQUEST_CODE_FINGERPRINT_PERMISSION = 9372;

    private static final String PREFS = "pin__preferences";

    private static final String PREF_UNLOCK_FAILURE_TIME = "pin__unlock_failure_time";
    private static final String PREF_UNLOCK_SUCCESS_TIME = "pin__unlock_success_time";

    protected Context context;

    protected CancellationSignal fingerprintCancellationSignal;
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

    public void attemptFingerprintUnlock(final UnlockDelegate eventListener) {
        if (handleFailureBlocking(eventListener))
            return;

        FingerprintUtils.authenticate(context, new FingerprintUtils.AuthenticationDelegate() {
            public void onHardwareNotPresent() {
                handleResolvableError(R.string.pin__fingerprint_error_none);
            }

            public void onPermissionNotGranted() {
                eventListener.onFingerprintPermissionRequired();
            }

            public void onNoFingerprints() {
                handleResolvableError(R.string.pin__fingerprint_error_none);
            }

            public void onServiceNotAvailable() {
                onHardwareNotPresent();
            }

            public void onAuthenticating(CancellationSignal cancellationSignal) {
                AppLock.this.fingerprintCancellationSignal = cancellationSignal;
            }

            public void onAuthenticationSuccess() {
                onUnlockSuccessful(eventListener);
            }

            private void handleResolvableError(int messageResId) {
                String message = context.getString(messageResId);

                if (eventListener != null)
                    eventListener.onUnlockError(message);
            }

            public void onAuthenticationFailed(String message) {
                handleUnlockFailure(message, eventListener);
            }
        });
    }

    public void attemptUnlock(String pin, final UnlockDelegate eventListener) {
        if (handleFailureBlocking(eventListener))
            return;

        PINUtils.authenticate(context, pin, new PINUtils.MatchEventListener() {
            public void onNoPIN() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_no_matching_pin_found));
            }

            public void onPINDoesNotMatch() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_match_failed));
            }

            public void onPINMatches() {
                onUnlockSuccessful(eventListener);
            }

            private void onUnlockFailed(String message) {
                handleUnlockFailure(message, eventListener);
            }
        });
    }

    /**
     * @return true if failure blocking is enabled
     */
    private boolean handleFailureBlocking(final UnlockDelegate eventListener) {
        if (isUnlockFailureBlockEnabled()) {
            retryCount++;

            if(getFailureDelayMs() < System.currentTimeMillis() - getUnlockFailureBlockStart())
                resetUnlockFailure();
            else{
                String message = String.format(
                        context.getString(R.string.pin__unlock_error_retry_limit_exceeded),
                        formatTimeRemaining());

                if (eventListener != null)
                    eventListener.onUnlockError(message);

                return true;
            }
        }

        return false;
    }

    private void handleUnlockFailure(String message, UnlockDelegate eventListener) {
        retryCount++;

        if (eventListener != null)
            eventListener.onUnlockError(message);

        if(context.getResources().getInteger(R.integer.pin__default_max_retry_count) < retryCount)
            onFailureExceedsLimit();
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

    protected void onUnlockSuccessful(UnlockDelegate eventListener) {
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
        resetUnlockFailure();

        getPreferences()
                .edit()
                .putLong(PREF_UNLOCK_SUCCESS_TIME, 0)
                .commit();

        PINUtils.removePIN(context);
        FingerprintUtils.removeAuthentications(context);
    }

    public void cancelPendingAuthentications() {
        if (fingerprintCancellationSignal != null) {
            this.fingerprintCancellationSignal.cancel();
            this.fingerprintCancellationSignal = null;
        }
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
        AppLock helper = getInstance(activity);

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
    public static void unlockIfRequired(Activity activity, @NonNull Runnable allowed, @Nullable Runnable canceled) {
        AppLock helper = getInstance(activity);

        if(helper.isUnlockRequired())
            new UnlockDialogBuilder(activity)
                    .onUnlocked(allowed)
                    .onCanceled(canceled)
                    .show();
        else
            allowed.run();
    }
}
