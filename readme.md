# App Lock

A simple library for locking and unlocking Activities (e.g. child lock) with a PIN code. 

![AppLock Sample](https://github.com/mattsilber/applock/raw/master/applock.gif)

# Installation

```
    repositories {
        jcenter()
    }

    dependencies {
        compile('com.guardanis:applock:1.0.7')
    }
```


# Usage

The goal of AppLock is to allow users to enter and confirm a PIN in order to temporarily lock the application from being used, until the PIN is re-entered by the user. To open the Activity to create a PIN, you can simply open the *AppLockActivity* via

```
    Intent intent = new Intent(activity, CreateLockActivity.class);
    startActivityForResult(intent, LockingHelper.REQUEST_CODE_CREATE_LOCK);
```

To check if a saved PIN exists, you can simply call *LockingHelper.hasSavedPIN(Activity)* and redirect to the *UnlockActivity* if the action requires PIN-authentication:

```
    Intent intent = new Intent(activity, UnlockActivity.class);
    startActivityForResult(intent, LockingHelper.REQUEST_CODE_ULOCK);    
```

If you want to do both of the above in a single step (that is, check if there's a saved PIN and open the unlock flow if yes), you can call:

```
    if(!ActionLockingHelper.unlockIfRequired(Activity))
        doSomethingThatRequiresLockingIfEnabled();


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LockingHelper.REQUEST_CODE_ULOCK)
            doSomethingThatRequiresLockingIfEnabled();
    }

```

If you want to do the above with a Dialog, instead of an Activity (which looks cooler, IMHO), you can simply call:

```
    ActionLockingHelper.unlockIfRequired(Activity, new UnlockEventListener(){
        public void onCanceled(){ } // Dialog was closed without entry
        public void onUnlockFailed(String reason){ } // Not called with default Dialog, instead is handled internally
        public void onUnlockSuccessful(){
            doSomethingThatRequiresLockingIfEnabled();
        }
    });
```

As of version 1.0.7, you can now create the lock PIN with a Dialog as well (why? because it looks cooler):
```
    new CreateLockDialogBuilder(Activity, 
        new LockCreationListener(){
            public void onLockCanceled(){ } // Dialog was closed without entry
            public void onLockSuccessful(){
                doSomethingOnAppLocked();
            }
        })
        .show();
```

If you want an Activity to remain fully locked once a PIN has been entered, ensure that you override *onPostResume()* and call *ActivityLockingHelper.onActivityResumed(Activity);* e.g.

```
    @Override
    protected void onPostResume(){
        super.onPostResume();
        ActivityLockingHelper.onActivityResumed(this);
    }
```

or you can simply have your Activity extend one of the Lockable Activites available in the library (*LockableCompatActivity* and *LockableActionBarActivity*)

By default, the ActivityLockingHelper considers a successful login as valid for 15 minutes, regardless of application state. You can shorten or extend that length by overriding the integer value for *pin__default_activity_lock_reenable_minutes* in your resources. Doing so will cause any Activity to re-open the *UnlockActivity* after the delay has passed. If you only want PIN-validation on a specific action (e.g. payments), you should use the ActionLockingHelper's methods posted above instead of locking the entre Activity.

To change the default length of the PIN, you can override

```
    <integer name="pin__default_input_count">4</integer>
```


# Theme

If you want to change the colors/themes, you can simply override the color values prefixed with *pin__*. For example, changing the ball/text colors from blue/white to green/yellow, respectively, could be done like so:

```
    <color name="pin__default_item_background">#2ECC71</color>
    <color name="pin__default_item_text">#F1C40F</color>
```

*(I wouldn't use that color mix, though. It's ugly. That was just for example)*

All colors you can change can be found in */values/colors.xml*

