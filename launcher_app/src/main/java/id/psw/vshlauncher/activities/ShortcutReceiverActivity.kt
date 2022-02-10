package id.psw.vshlauncher.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import id.psw.vshlauncher.R
import id.psw.vshlauncher.views.showDialog
import id.psw.vshlauncher.vsh
import id.psw.vshlauncher.xmb

class ShortcutReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val pinItem = launcherApps.getPinItemRequest(intent)
            val xmb = vsh.xmbView
            if(xmb != null){
                // TODO: xmb.showDialog()
            }
        } else {
            AlertDialog.Builder(this).setTitle(R.string.sdk_widget_shortcut_not_supported)
                .setNegativeButton(android.R.string.ok) { a, b ->
                    a.dismiss()
                    this.finish()
                }.show()
        }
    }
}