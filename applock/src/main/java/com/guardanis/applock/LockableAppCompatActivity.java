package com.guardanis.applock;

import android.support.v7.app.AppCompatActivity;

public class LockableAppCompatActivity extends AppCompatActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();

        AppLock.onActivityResumed(this);
    }
}

