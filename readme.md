# App Lock

A simply library for locking and unlocking Activities (e.g. child lock) with a PIN code. (screenshots coming soon)

# Installation

```
    repositories {
        maven { url "http://dl.bintray.com/mattsilber/maven" }
    }

    dependencies {
        compile('com.guardanis:applock:1.0.0')
    }
```

I should have it on *jcenter/mavenCentral* soon enough, but apparently that takes a bit...

# Usage

The goal of AppLock is to allow users to enter and confirm a PIN in order to temporarily lock the application from being used, until the PIN is re-entered by the user. To open the Activity to create a PIN, you can simply open the *AppLockActivity* via

```
    Intent intent = new Intent(activity, AppLockActivity.class);
    activity.startActivity(intent);
```

To ensure an Activity remains locked once a PIN has been entered, ensure that you override *onPostResume()* and call *LockingHelper.onActivityResumed(Activity);* e.g.

```
    @Override
    protected void onPostResume(){
        super.onPostResume();
        LockingHelper.onActivityResumed(this);
    }
```

or you can simply have your Activity extend one of the Lockable Activites available in the library (*LockableCompatActivity* and *LockableActionBarActivity*)

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

