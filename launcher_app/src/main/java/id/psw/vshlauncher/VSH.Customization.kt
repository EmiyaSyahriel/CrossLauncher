package id.psw.vshlauncher

import android.content.Intent


fun VSH.isXPKGIntent(intent:Intent) : Boolean{
    return intent.action == Intent.ACTION_VIEW && intent.data?.path?.endsWith(".xpkg") == true
}

fun VSH.showInstallPkgDialog(intent: Intent) {

}