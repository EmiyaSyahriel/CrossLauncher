package id.psw.vshlauncher.activities

import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import id.psw.vshlauncher.views.dialogviews.InstallShortcutDialogView
import id.psw.vshlauncher.vsh

fun Xmb.isCreateShortcutIntent(intent: Intent?) : Boolean{
    if(intent != null){
        var retval = intent.action?.contentEquals(Intent.ACTION_CREATE_SHORTCUT) == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    retval = retval|| intent.action?.contentEquals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) == true
        }
        return retval
    }
    return false
}

fun Xmb.showShortcutCreationDialog(intent:Intent?){
    if(intent != null){
        xmbView.showDialog(InstallShortcutDialogView(xmbView, intent))
    }
}