package id.psw.vshlauncher.activities

import android.content.Intent
import id.psw.vshlauncher.*
import id.psw.vshlauncher.views.dialogviews.SetWallpaperDialogView

val Intent.isShareIntent : Boolean get() = action == Intent.ACTION_SEND

fun Xmb.showShareIntentDialog(intent:Intent) {
    try{
        xmbView.showDialog(SetWallpaperDialogView(xmbView, this, intent))
    }catch(e:Exception) {
        vsh.postNotification(null, "Failed to Set Wallpaper",e.toString(), 5.0f)
        xmbView.switchScreen(xmbView.screens.mainMenu)
    }
}