package com.guardanis.applock.activities;

import android.support.v7.app.AppCompatActivity;

import com.guardanis.applock.AppLock;

public class LockableAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();

        AppLock.onActivityResumed(this);
    }
}

