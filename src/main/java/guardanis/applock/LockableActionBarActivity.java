package guardanis.applock;

import android.support.v7.app.ActionBarActivity;

public class LockableActionBarActivity extends ActionBarActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();
        LockingHelper.onActivityResumed(this);
    }

}

