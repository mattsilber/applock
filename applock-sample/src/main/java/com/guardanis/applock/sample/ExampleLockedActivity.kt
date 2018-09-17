package com.guardanis.applock.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.guardanis.applock.AppLock
import com.guardanis.applock.activities.LockableAppCompatActivity

public class ExampleLockedActivity: LockableAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample__activity_locked_example)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AppLock.REQUEST_CODE_LOCK_CREATION -> {
                if (resultCode == Activity.RESULT_OK)
                    Toast.makeText(this, "Lock created!", Toast.LENGTH_SHORT)
                            .show()
            }
        }
    }
}