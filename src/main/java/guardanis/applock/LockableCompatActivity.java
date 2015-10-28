package guardanis.applock;

import android.support.v7.app.AppCompatActivity;

public class LockableCompatActivity extends AppCompatActivity {

    @Override
    protected void onPostResume(){
        super.onPostResume();
        LockingHelper.onActivityResumed(this);
    }

}

