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

    fun openApplockFlowClicked(view: View?) {
        if (!AppLock.isUnlockMethodPresent(this)) {
            showCreateLockFlow()
            return
        }

        showUnlockFlow()
    }

    fun showCreateLockFlow() {
        CreateLockDialogBuilder(this)
                .onCanceled({ showIndicatorMessage("You canceled...") })
                .onLockCreated({ showIndicatorMessage("Lock created!") })
                .show()
    }

    fun showUnlockFlow() {
        val helper = AppLock.getInstance(this)

        if (helper.isUnlockRequired(1))
            UnlockDialogBuilder(this)
                    .onCanceled({ showIndicatorMessage("Unlock canceled!") })
                    .onUnlocked({
                        showIndicatorMessage("Unlock success! Lock removed.")

                        AppLock.getInstance(this@MainActivity)
                                .clearData()
                    })
                    .show()
    }

    private fun showIndicatorMessage(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                .show()
    }
}
