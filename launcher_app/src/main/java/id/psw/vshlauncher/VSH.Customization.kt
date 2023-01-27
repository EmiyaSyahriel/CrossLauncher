package id.psw.vshlauncher

import android.content.Intent
import id.psw.vshlauncher.views.dialogviews.InstallPackageDialogView
import id.psw.vshlauncher.views.showDialog


inline fun VSH.isXPKGIntent(intent:Intent) : Boolean{
    val isView = intent.action == Intent.ACTION_VIEW
    val path = intent.data?.path ?: ""
    return isView && path.endsWith(".xpkg")
}

inline fun VSH.showInstallPkgDialog(intent: Intent) {
    xmbView?.showDialog(InstallPackageDialogView(this, intent))
}