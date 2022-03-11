package id.psw.vshlauncher.activities

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.views.dialogviews.InstallShortcutDialogView
import id.psw.vshlauncher.views.showDialog
import id.psw.vshlauncher.vsh

fun XMB.isCreateShortcutIntent(intent: Intent?) : Boolean{
    if(intent != null){
        var retval = intent.action?.contentEquals(Intent.ACTION_CREATE_SHORTCUT) == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    retval = retval|| intent.action?.contentEquals(LauncherApps.ACTION_CONFIRM_PIN_SHORTCUT) == true
        }
        return retval
    }
    return false
}

fun XMB.showShortcutCreationDialog(intent:Intent?){
    if(intent != null){
        xmbView.showDialog(InstallShortcutDialogView(vsh, intent))
    }
}