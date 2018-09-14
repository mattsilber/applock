package com.guardanis.applock.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.guardanis.applock.AppLock
import com.guardanis.applock.dialogs.CreateLockDialogBuilder.LockCreationListener
import android.widget.Toast
import com.guardanis.applock.dialogs.CreateLockDialogBuilder
import com.guardanis.applock.dialogs.UnlockDialogBuilder.UnlockEventListener
import com.guardanis.applock.utils.PINUtils

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
    }

    fun openApplockFlowClicked(view: View?) {
        if (!AppLock.isUnlockMethodPresent(this)) {
            showCreateLockFlow()
            return
        }

        showUnlockFlow()
    }

    fun showCreateLockFlow() {
        CreateLockDialogBuilder(
                this,
                object : LockCreationListener {

                    override fun onLockCanceled() {
                        Toast.makeText(this@MainActivity, "You canceled...", Toast.LENGTH_SHORT)
                                .show()
                    }

                    override fun onLockSuccessful() {
                        Toast.makeText(this@MainActivity, "Lock created!", Toast.LENGTH_SHORT)
                                .show()
                    }
                })
                .show()
    }

    fun showUnlockFlow() {
        AppLock.unlockIfRequired(
                this,
                object: UnlockEventListener {

                    override fun onCanceled() {
                        Toast.makeText(this@MainActivity, "Unlock canceled!", Toast.LENGTH_SHORT)
                                .show()
                    }

                    override fun onUnlockFailed(reason: String) { } // Not called with default Dialog, instead is handled internally

                    override fun onUnlockSuccessful() {
                        Toast.makeText(this@MainActivity, "Lock removed!", Toast.LENGTH_SHORT)
                                .show()

                        AppLock.getInstance(this@MainActivity)
                                .clearData()
                    }
        })
    }
}
