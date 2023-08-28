package id.psw.vshlauncher.types.items

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.UserHandle
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.reloadShortcutList
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.CIFLoader
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.XMBShortcutInfo
import id.psw.vshlauncher.types.sequentialimages.XMBFrameAnimation
import id.psw.vshlauncher.views.dialogviews.ConfirmDialogView
import id.psw.vshlauncher.views.showDialog
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
    private val sourceAppName : String = try {
        val appInfo = if (sdkAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            vsh.packageManager.getApplicationInfo(shortcut.packageName, PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            vsh.packageManager.getApplicationInfo(shortcut.packageName, 0)
        }
        appInfo.loadLabel(vsh.packageManager).toString()
    }catch(_:Exception){
        shortcut.longName
    }

    override val displayName: String
        get() = shortcut.name

    override val hasDescription: Boolean
        get() = true


    override val icon: Bitmap get() = cif.icon.bitmap
    override val hasIcon: Boolean  get() = true
    override val isIconLoaded: Boolean get() = cif.hasIconLoaded
    override val animatedIcon: XMBFrameAnimation get() = cif.animIcon
    override val hasAnimatedIcon: Boolean get() = cif.hasAnimatedIcon
    override val isAnimatedIconLoaded: Boolean get() = cif.hasAnimIconLoaded

    override val hasBackSound: Boolean get()= cif.hasBackSound

    override val backSound: File
        get() = cif.backSound

    override val hasMenu: Boolean
        get() = true

    private val menus = arrayListOf<XMBMenuItem>().apply {
        val that = this@XMBShortcutItem
        add(XMBMenuItem.XMBMenuItemLambda(
            { vsh.getString(R.string.shortcut_launch) },
            { false },
            0
        ){
            launch(that)
        })
        add(XMBMenuItem.XMBMenuItemLambda(
            { vsh.getString(R.string.shortcut_remove) },
            { false },
            1
        ){
            deleteItem()

        })
    }

    private fun deleteItem(){
        vsh.xmbView?.showDialog(ConfirmDialogView(vsh, vsh.getString(R.string.shortcut_remove),
            R.drawable.ic_error, // TODO: Use trash icon
            vsh.getString(R.string.shortcut_remove_confirm_text).format(displayName)
        ){confirmed ->
            if(confirmed) {
                file.delete()
                vsh.reloadShortcutList()
            } // Do nothing
        })
    }

    override val menuItems: ArrayList<XMBMenuItem>
        get() = menus

    override val description: String
        get() = sourceAppName

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