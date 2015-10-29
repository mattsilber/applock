package com.guardanis.applock;

import android.support.v7.app.AppCompatActivity;

import com.guardanis.applock.locking.ActivityLockingHelper;

public class LockableCompatActivity extends AppCompatActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();
        ActivityLockingHelper.onActivityResumed(this);
    }

}

