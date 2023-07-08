package id.psw.vshlauncher.types.items

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.os.Build
import android.os.UserHandle
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.delayedExistenceCheck
import id.psw.vshlauncher.getOrMake
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.types.CIFLoader
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.types.INIFile
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XMBShortcutInfo
import id.psw.vshlauncher.types.sequentialimages.XMBFrameAnimation
import java.io.File

class XMBShortcutItem(val vsh: VSH, private val file: File) : XMBItem(vsh) {

    private val shortcut = XMBShortcutInfo(vsh, file)

    override val onLaunch: (XMBItem) -> Unit = {
        launch(it)
    }

    override val onScreenVisible: (XMBItem) -> Unit = { cif.loadIcon() }
    override val onScreenInvisible: (XMBItem) -> Unit = { cif.unloadIcon() }
    override val onHovered: (XMBItem) -> Unit = { cif.apply { loadBackdrop(); loadSound(); } }
    override val onUnHovered: (XMBItem) -> Unit = { cif.apply { unloadBackdrop(); unloadSound(); } }

    private val cif = CIFLoader(vsh, shortcut.idInLauncher, file.parentFile!!)

    override val displayName: String
        get() = shortcut.name

    override val icon: Bitmap get() = cif.icon.bitmap
    override val hasIcon: Boolean  get() = true
    override val isIconLoaded: Boolean get() = cif.hasIconLoaded
    override val animatedIcon: XMBFrameAnimation get() = cif.animIcon
    override val hasAnimatedIcon: Boolean get() = cif.hasAnimatedIcon
    override val isAnimatedIconLoaded: Boolean get() = cif.hasAnimIconLoaded

    override val hasBackSound: Boolean get()= cif.hasBackSound

    override val backSound: File
        get() = cif.backSound


    override val description: String
        get() = shortcut.longName

    override val id: String
        get() = shortcut.idInLauncher

    val category : String get() = shortcut.category

    private fun launch(x:XMBItem){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val lcher = vsh.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val uid = android.os.Process.myUid()
            var success = false
            try{
                lcher.startShortcut(shortcut.packageName, shortcut.id, vsh.xmbView?.clipBounds, null, UserHandle.getUserHandleForUid(uid))
                success = true
            }catch(_:Exception){}

            if(!success){
                vsh.postNotification(R.drawable.ic_error,
                    vsh.getString(R.string.shortcut_failed_to_launch_title),
                    vsh.getString(R.string.shortcut_failed_to_launch_desc).format(shortcut.packageName))
            }
        }
    }

    init {

    }
}