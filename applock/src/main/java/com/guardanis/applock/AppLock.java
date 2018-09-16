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
import com.guardanis.applock.services.FingerprintLockService;
import com.guardanis.applock.services.LockService;
import com.guardanis.applock.services.PINLockService;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class AppLock {

    public interface UnlockDelegate {
        public void onUnlockSuccessful();
        public void onResolutionRequired(int errorCode);
        public void onAuthenticationHelp(int code, String message);
        public void onFailureLimitExceeded(String message);
    }

    private static AppLock instance;
    public static AppLock getInstance(Context context) {
        if (instance == null)
            instance = new AppLock(context);

        return instance;
    }

    public static final int REQUEST_CODE_UNLOCK = 9371;
    public static final int REQUEST_CODE_LOCK_CREATION = 9372;
    public static final int REQUEST_CODE_FINGERPRINT_PERMISSION = 9373;

    private static final String PREFS = "pin__preferences";

    private static final String PREF_UNLOCK_FAILURE_TIME = "pin__unlock_failure_time";
    private static final String PREF_UNLOCK_SUCCESS_TIME = "pin__unlock_success_time";

    public static final int ERROR_CODE_FINGERPRINTS_MISSING_HARDWARE = 1;
    public static final int ERROR_CODE_FINGERPRINTS_PERMISSION_REQUIRED = 2;
    public static final int ERROR_CODE_FINGERPRINTS_EMPTY = 3;
    public static final int ERROR_CODE_FINGERPRINTS_NOT_LOCALLY_ENROLLED = 4;

    protected Context context;

    protected HashMap<Class, LockService> lockServices = new HashMap<Class, LockService>();

    protected int retryCount = 1;

    protected AppLock(Context context){
        this.context = context.getApplicationContext();

        this.lockServices.put(PINLockService.class, new PINLockService());
        this.lockServices.put(FingerprintLockService.class, new FingerprintLockService());
    }

    public static boolean isEnrolled(Context context) {
        AppLock helper = getInstance(context);

        for (LockService service : helper.lockServices.values()) {
            if (service.isEnrolled(context))
                return true;
        }

        return false;
    }

    public static boolean isUnlockRequired(Context context) {
        AppLock helper = getInstance(context);

        int minutes = context.getResources()
                .getInteger(R.integer.pin__default_activity_lock_reenable_minutes);

        return helper.isUnlockRequired(TimeUnit.MINUTES.toMillis(minutes));
    }

    public boolean isUnlockRequired(long lastSuccessValidMs) {
        return isEnrolled(context) && lastSuccessValidMs < System.currentTimeMillis() - getUnlockSuccessTime();
    }

    public void attemptFingerprintUnlock(boolean localEnrollmentRequired, final UnlockDelegate eventListener) {
        if (handleFailureBlocking(eventListener))
            return;

        FingerprintLockService.AuthenticationDelegate delegate = new FingerprintLockService.AuthenticationDelegate() {
            @Override
            public void onResolutionRequired(int errorCode) {
                eventListener.onResolutionRequired(errorCode);
            }

            @Override
            public void onAuthenticationHelp(int code, CharSequence message) {
                eventListener.onAuthenticationHelp(code, String.valueOf(message));
            }

            @Override
            public void onAuthenticating(CancellationSignal cancellationSignal) {
                // Handled internally
            }

            @Override
            public void onAuthenticationSuccess() {
                onUnlockSuccessful(eventListener);
            }

            @Override
            public void onAuthenticationFailed(String message) {
                handleUnlockFailure(message, eventListener);
            }
        };

        getLockService(FingerprintLockService.class)
                    .authenticate(context, localEnrollmentRequired, delegate);
    }

    public void attemptPINUnlock(String pin, final UnlockDelegate eventListener) {
        if (handleFailureBlocking(eventListener))
            return;

        PINLockService.AuthenticationDelegate delegate = new PINLockService.AuthenticationDelegate() {
            @Override
            public void onNoPIN() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_no_matching_pin_found));
            }

            @Override
            public void onPINDoesNotMatch() {
                onUnlockFailed(context.getString(R.string.pin__unlock_error_match_failed));
            }

            @Override
            public void onPINMatches() {
                onUnlockSuccessful(eventListener);
            }

            private void onUnlockFailed(String message) {
                handleUnlockFailure(message, eventListener);
            }
        };

        getLockService(PINLockService.class)
                .authenticate(context, pin, delegate);
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
                    eventListener.onFailureLimitExceeded(message);

                return true;
            }
        }

        return false;
    }

    private void handleUnlockFailure(String message, UnlockDelegate eventListener) {
        retryCount++;

        if (eventListener != null)
            eventListener.onFailureLimitExceeded(message);

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

        for (LockService service : lockServices.values())
            service.invalidateEnrollment(context);
    }

    public void cancelPendingAuthentications() {
        for (LockService service : lockServices.values())
            service.cancelPendingAuthentications(context);
    }

    public <T extends LockService> T getLockService(Class<T> named) {
        return (T) lockServices.get(named);
    }

    public static void onActivityResumed(Activity activity) {
        if(isUnlockRequired(activity)){
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
        if(isUnlockRequired(activity)){
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
        if(isUnlockRequired(activity))
            new UnlockDialogBuilder(activity)
                    .onUnlocked(allowed)
                    .onCanceled(canceled)
                    .show();
        else
            allowed.run();
    }
}
