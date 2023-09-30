package id.psw.vshlauncher

import android.content.Intent
import id.psw.vshlauncher.views.dialogviews.InstallPackageDialogView

fun Vsh.isXPKGIntent(intent:Intent) : Boolean{
    val isView = intent.action == Intent.ACTION_VIEW
    val path = intent.data?.path ?: ""
    return isView && path.endsWith(".xpkg")
}

fun Vsh.showInstallPkgDialog(intent: Intent) {
    if(haveXmbView){
        safeXmbView.showDialog(InstallPackageDialogView(safeXmbView, intent))
    }
}