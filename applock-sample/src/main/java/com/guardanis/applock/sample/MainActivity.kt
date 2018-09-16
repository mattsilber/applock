package com.guardanis.applock.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.guardanis.applock.AppLock
import com.guardanis.applock.AppLock.REQUEST_CODE_LOCK_CREATION
import com.guardanis.applock.AppLock.REQUEST_CODE_UNLOCK
import com.guardanis.applock.activities.LockCreationActivity
import com.guardanis.applock.activities.UnlockActivity
import com.guardanis.applock.dialogs.LockCreationDialogBuilder
import com.guardanis.applock.dialogs.UnlockDialogBuilder

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AppLock.REQUEST_CODE_UNLOCK -> {
                if (resultCode == Activity.RESULT_OK)
                    clearLocks()
            }
        }
    }

    fun dialogFlowClicked(view: View?) {
        if (!AppLock.isUnlockMethodPresent(this)) {
            showDialogCreateLockFlow()
            return
        }

        showDialogUnlockFlow()
    }

    private fun showDialogCreateLockFlow() {
        LockCreationDialogBuilder(this)
                .onCanceled({ showIndicatorMessage("You canceled...") })
                .onLockCreated({ showIndicatorMessage("Lock created!") })
                .show()
    }

    private fun showDialogUnlockFlow() {
        val helper = AppLock.getInstance(this)

        if (helper.isUnlockRequired(1))
            UnlockDialogBuilder(this)
                    .onCanceled({ showIndicatorMessage("Unlock canceled!") })
                    .onUnlocked({ clearLocks() })
                    .show()
    }

    fun activityFlowClicked(view: View?) {
        if (!AppLock.isUnlockMethodPresent(this)) {
            showActivityCreateLockFlow()
            return
        }

        showActivityUnlockFlow()
    }

    private fun showActivityCreateLockFlow() {
        val intent = Intent(this, LockCreationActivity::class.java)
                .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, true)

        startActivityForResult(intent, REQUEST_CODE_LOCK_CREATION)
    }

    private fun showActivityUnlockFlow() {
        val intent = Intent(this, UnlockActivity::class.java)
                .putExtra(UnlockActivity.INTENT_ALLOW_UNLOCKED_EXIT, false)

        startActivityForResult(intent, REQUEST_CODE_UNLOCK)
    }

    private fun clearLocks() {
        showIndicatorMessage("Unlock success! Lock removed.")

        AppLock.getInstance(this@MainActivity)
                .clearData()
    }

    private fun showIndicatorMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                .show()
    }
}
