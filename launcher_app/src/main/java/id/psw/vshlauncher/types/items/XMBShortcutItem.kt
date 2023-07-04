package id.psw.vshlauncher.types.items

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import android.os.UserHandle
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.types.INIFile
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XMBShortcutInfo
import java.io.File

class XMBShortcutItem(val vsh: VSH, file: File) : XMBItem(vsh) {

    private val shortcut = XMBShortcutInfo(vsh, file)

    override val onLaunch: (XMBItem) -> Unit = {
        launch(it)
    }

    override val displayName: String
        get() = shortcut.longName

    override val icon: Bitmap
        get() = shortcut.icon

    override val description: String
        get() = shortcut.longName

    val category : String get() = shortcut.category

    fun launch(x:XMBItem){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val lcher = vsh.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val uid = android.os.Process.myUid()
            lcher.startShortcut(shortcut.packageName, shortcut.id, null, null, UserHandle.getUserHandleForUid(uid))
        }
    }

    init {

    }
}