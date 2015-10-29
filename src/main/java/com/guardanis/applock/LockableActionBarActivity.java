package com.guardanis.applock;

import android.support.v7.app.ActionBarActivity;

import com.guardanis.applock.locking.ActivityLockingHelper;

public class LockableActionBarActivity extends ActionBarActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();
        ActivityLockingHelper.onActivityResumed(this);
    }

}

