package com.guardanis.applock.locking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.guardanis.applock.R;

import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public abstract class LockingHelper {

    public interface LockEventListener {
        public void onUnlockSuccessful();

        public void onUnlockFailed(String reason);
    }

    public static final int REQUEST_CODE_UNLOCK = 93371;

    private static final String PREF_SAVED_LOCKED_PASSWORD = "pin__saved_locked_password";
    private static final String PREF_UNLOCK_FAILURE_TIME = "pin__unlock_failure_time";
    private static final String PREF_UNLOCK_SUCCESS_TIME = "pin__unlock_success_time";

    protected Activity activity;
    protected LockEventListener eventListener;

    protected int retryCount = 1;

    protected LockingHelper(Activity activity){
        this(activity, null);
    }

    public LockingHelper(Activity activity, LockEventListener eventListener) {
        this.activity = activity;
        this.eventListener = eventListener;
    }

    public abstract boolean isUnlockRequired();

    public void attemptUnlock(String pin) {
        if(!isUnlockRequired()){
            eventListener.onUnlockFailed(activity.getString(R.string.pin__unlock_error_no_matching_pin_found));
            return;
        }
        else if(isUnlockFailureBlockEnabled()){
            retryCount++;

            if(getFailureDelayMs() < System.currentTimeMillis() - getUnlockFailureBlockStart())
                resetUnlockFailure();
            else{
                eventListener.onUnlockFailed(String.format(activity.getString(R.string.pin__unlock_error_retry_limit_exceeded), formatTimeRemaining()));
                return;
            }
        }

        if(encrypt(pin).equals(getSavedLockPIN()))
            onUnlockSuccessful();
        else{
            retryCount++;
            eventListener.onUnlockFailed(activity.getString(R.string.pin__unlock_error_match_failed));

            if(activity.getResources().getInteger(R.integer.pin__default_max_retry_count) < retryCount)
                onFailureExceedsLimit();
        }
    }

    protected SharedPreferences getSavedLockPreference(){
        return activity.getSharedPreferences(LockingHelper.class.getName(), 0);
    }

    protected String getSavedLockPIN() {
        return getSavedLockPreference()
                .getString(PREF_SAVED_LOCKED_PASSWORD, null);
    }

    public void saveLockPIN(String pin) {
        getSavedLockPreference()
                .edit()
                .putString(PREF_SAVED_LOCKED_PASSWORD, encrypt(pin))
                .commit();
    }

    public void removeSavedLockPIN() {
        getSavedLockPreference()
                .edit()
                .remove(PREF_SAVED_LOCKED_PASSWORD)
                .commit();
    }

    protected String encrypt(String text) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes("UTF-8"), 0, text.length());
            byte[] sha1hash = md.digest();
            return convertToHex(sha1hash);
        }
        catch(Exception e){ e.printStackTrace(); }
        return "";
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for(byte b : data){
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do{
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    protected void onFailureExceedsLimit(){
        getSavedLockPreference()
                .edit()
                .putLong(PREF_UNLOCK_FAILURE_TIME, System.currentTimeMillis())
                .commit();
    }

    public boolean isUnlockFailureBlockEnabled() {
        return activity.getResources().getInteger(R.integer.pin__default_max_retry_count) < retryCount
                || System.currentTimeMillis() - getUnlockFailureBlockStart() < getFailureDelayMs();
    }

    protected long getUnlockFailureBlockStart() {
        return getSavedLockPreference().getLong(PREF_UNLOCK_FAILURE_TIME, 0);
    }

    protected void onUnlockSuccessful(){
        getSavedLockPreference()
                .edit()
                .putLong(PREF_UNLOCK_SUCCESS_TIME, System.currentTimeMillis())
                .commit();

        resetUnlockFailure();
        eventListener.onUnlockSuccessful();
    }

    protected long getUnlockSuccessTime() {
        return getSavedLockPreference().getLong(PREF_UNLOCK_SUCCESS_TIME, 0);
    }

    protected void resetUnlockFailure() {
        retryCount = 1;

        getSavedLockPreference()
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
        return TimeUnit.MINUTES.toMillis(activity.getResources().getInteger(R.integer.pin__default_failure_retry_delay));
    }

}
