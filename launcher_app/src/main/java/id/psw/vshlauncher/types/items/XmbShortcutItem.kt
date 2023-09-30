package id.psw.vshlauncher.types.items

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.UserHandle
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.reloadShortcutList
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.CifLoader
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.XmbShortcutInfo
import id.psw.vshlauncher.types.sequentialimages.XmbFrameAnimation
import id.psw.vshlauncher.views.dialogviews.ConfirmDialogView
import id.psw.vshlauncher.views.nativedlg.NativeEditTextDialog
import java.io.File

class XmbShortcutItem(val vsh: Vsh, private val file: File) : XmbItem(vsh) {

    private val shortcut = XmbShortcutInfo(vsh, file)

    override val onLaunch: (XmbItem) -> Unit = {
        launch(it)
    }

    override val onScreenVisible: (XmbItem) -> Unit = { cif.loadIcon() }
    override val onScreenInvisible: (XmbItem) -> Unit = { cif.unloadIcon() }
    override val onHovered: (XmbItem) -> Unit = { cif.apply { loadBackdrop(); loadSound(); } }
    override val onUnHovered: (XmbItem) -> Unit = { cif.apply { unloadBackdrop(); unloadSound(); } }

    private val cif = CifLoader(vsh, shortcut.idInLauncher, file.parentFile!!)
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

    override val displayName: String get() = shortcut.name
    override val hasDescription: Boolean get() = true
    override val icon: Bitmap get() = cif.icon.bitmap
    override val hasIcon: Boolean  get() = true
    override val isIconLoaded: Boolean get() = cif.hasIconLoaded
    override val animatedIcon: XmbFrameAnimation get() = cif.animIcon
    override val hasAnimatedIcon: Boolean get() = cif.hasAnimatedIcon
    override val isAnimatedIconLoaded: Boolean get() = cif.hasAnimIconLoaded
    override val hasBackSound: Boolean get()= cif.hasBackSound
    override val backSound: File get() = cif.backSound
    override val hasMenu: Boolean get() = true
    override val menuItems: ArrayList<XmbMenuItem> get() = menus
    override val description: String get() = sourceAppName
    override val id: String get() = shortcut.idInLauncher
    val category : String get() = shortcut.category

    private val menus = arrayListOf<XmbMenuItem>().apply {
        val that = this@XmbShortcutItem
        add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.shortcut_launch) },
            { false },
            0
        ){
            launch(that)
        })

        add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.shortcut_rename) },
            { false },
            1
        ){
            NativeEditTextDialog(vsh)
                .setTitle(vsh.getString(R.string.dlg_info_rename))
                .setValue(shortcut.name)
                .setOnFinish {
                    shortcut.name = it
                    shortcut.write(file)
                }
                .show()
        })
        add(XmbMenuItem.XmbMenuItemLambda(
            { vsh.getString(R.string.shortcut_remove) },
            { false },
            2
        ){
            deleteItem()
        })
    }

    private fun deleteItem(){
        val xv = vsh.xmbView
        xv?.showDialog(ConfirmDialogView(xv, vsh.getString(R.string.shortcut_remove),
            R.drawable.ic_error, // TODO: Use trash icon
            vsh.getString(R.string.shortcut_remove_confirm_text).format(displayName)
        ){confirmed ->
            if(confirmed) {
                file.delete()
                vsh.reloadShortcutList()
            } // Do nothing
        })
    }


    private fun launch(x:XmbItem){
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
}