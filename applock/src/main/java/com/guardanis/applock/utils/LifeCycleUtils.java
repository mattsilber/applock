package com.guardanis.applock.utils;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import java.lang.ref.WeakReference;

public class LifeCycleUtils {

    public static final String ACTION_NOTIFY_ACTIVITY_RESUME = "com.guardanis.applock.activity_resume";
    public static final String ACTION_NOTIFY_ACTIVITY_PAUSE = "com.guardanis.applock.activity_pause";

    public static IntentFilter buildIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NOTIFY_ACTIVITY_RESUME);
        filter.addAction(ACTION_NOTIFY_ACTIVITY_PAUSE);

        return filter;
    }

    public static AppLockActivityLifeCycleCallbacks attach(Activity activity, BroadcastReceiver receiver, IntentFilter targetIntentFilters) {
        activity.registerReceiver(receiver, targetIntentFilters);

        AppLockActivityLifeCycleCallbacks callbacks = new AppLockActivityLifeCycleCallbacks(activity, receiver, targetIntentFilters);

        activity.getApplication()
                .registerActivityLifecycleCallbacks(callbacks);

        return callbacks;
    }

    protected static class AppLockActivityLifeCycleCallbacks implements Application.ActivityLifecycleCallbacks {

        protected WeakReference<Activity> openedActivity;
        protected WeakReference<BroadcastReceiver> targetReceiver;
        protected IntentFilter targetIntentFilters;

        public AppLockActivityLifeCycleCallbacks(Activity activity, BroadcastReceiver receiver, IntentFilter targetIntentFilters) {
            this.openedActivity = new WeakReference<Activity>(activity);
            this.targetReceiver = new WeakReference<BroadcastReceiver>(receiver);
            this.targetIntentFilters = targetIntentFilters;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) { }

        @Override
        public void onActivityStarted(Activity activity) { }

        @Override
        public void onActivityStopped(Activity activity) { }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) { }

        @Override
        public void onActivityResumed(Activity activity) {
            Activity opened = this.openedActivity.get();
            BroadcastReceiver receiver = this.targetReceiver.get();

            if (opened == null || receiver == null || opened != activity)
                return;

            opened.registerReceiver(receiver, targetIntentFilters);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Activity opened = this.openedActivity.get();
            BroadcastReceiver receiver = this.targetReceiver.get();

            if (opened == null || receiver == null || opened != activity)
                return;

            opened.unregisterReceiver(receiver);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Activity opened = this.openedActivity.get();

            if (opened == null || opened != activity)
                return;

            opened.getApplication()
                    .unregisterActivityLifecycleCallbacks(this);
        }
    }
}
