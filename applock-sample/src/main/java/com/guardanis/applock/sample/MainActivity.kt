package com.guardanis.applock.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.guardanis.applock.AppLock
import android.widget.Toast
import com.guardanis.applock.dialogs.AppLockDialogBuilder
import com.guardanis.applock.dialogs.CreateLockDialogBuilder
import com.guardanis.applock.dialogs.UnlockDialogBuilder
import com.guardanis.applock.utils.PINUtils

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)

        setContentView(R.layout.activity_main)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            AppLock.REQUEST_CODE_FINGERPRINT_PERMISSION -> {
                val intent = Intent()
                        .setAction(AppLockDialogBuilder.ACTION_NOTIFY_PERMISSION_CHANGE)

                sendBroadcast(intent)
            }
        }
    }

    fun openApplockFlowClicked(view: View?) {
        if (!AppLock.isUnlockMethodPresent(this)) {
            showCreateLockFlow()
            return
        }

        showUnlockFlow()
    }

    fun showCreateLockFlow() {
        CreateLockDialogBuilder(this)
                .onCanceled({
                    Toast.makeText(this@MainActivity, "You canceled...", Toast.LENGTH_SHORT)
                            .show()
                })
                .onLockCreated({
                    Toast.makeText(this@MainActivity, "Lock created!", Toast.LENGTH_SHORT)
                            .show()
                })
                .show()
    }

    fun showUnlockFlow() {
        val helper = AppLock.getInstance(this)

        if (helper.isUnlockRequired())
            UnlockDialogBuilder(this)
                    .onCanceled(null)
                    .onUnlocked({
                        Toast.makeText(this@MainActivity, "Lock removed!", Toast.LENGTH_SHORT)
                                .show()

                        AppLock.getInstance(this@MainActivity)
                                .clearData()
                    })
                    .show()
    }
}
