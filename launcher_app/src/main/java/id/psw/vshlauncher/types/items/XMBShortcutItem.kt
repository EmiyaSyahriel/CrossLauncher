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

    override val onScreenVisible: (XMBItem) -> Unit = { loadIcon(it) }
    override val onScreenInvisible: (XMBItem) -> Unit = { unloadIcon(it) }
    override val onHovered: (XMBItem) -> Unit = { loadBackdrop(it) }
    override val onUnHovered: (XMBItem) -> Unit = { unloadBackdrop(it) }

    private var _icon = BitmapRef("TRANSPARENT_BITMAP", { TRANSPARENT_BITMAP })
    private var _backdrop = BitmapRef("TRANSPARENT_BITMAP", { TRANSPARENT_BITMAP })
    private var _animIcon : XMBFrameAnimation = TRANSPARENT_ANIM_BITMAP
    private var _animIconLoading = false
    private var _animIconHas = false
    private var _backSound : File = SILENT_AUDIO
    private val ios = mutableMapOf<File, Ref<Boolean>>()
    private val ioc = mutableMapOf<File, Ref<Int>>()
    private fun <K> MutableMap<File, Ref<K>>.getOrMake(k:File, refDefVal:K) = getOrMake<File, Ref<K>>(k){ Ref<K>(refDefVal) }

    override val displayName: String
        get() = shortcut.name

    override val icon: Bitmap get() = _icon.bitmap
    override val hasIcon: Boolean  get() = _icon.id != "TRANSPARENT_BITMAP"
    override val isIconLoaded: Boolean get() = !_icon.isLoading

    override val animatedIcon: XMBFrameAnimation get() = _animIcon
    override val hasAnimatedIcon: Boolean get() = _animIconHas
    override val isAnimatedIconLoaded: Boolean get() = _animIconLoading

    private var _backSoundList : ArrayList<File> = FileQuery(file.parent!!, true)
        .withNames(file.nameWithoutExtension)
        .withExtensionArray(VshResTypes.SOUNDS)
        .execute(vsh)

    override val hasBackSound: Boolean
        get() = _backSoundList.any {
            it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
        }

    override val backSound: File
        get() = _backSound


    override val description: String
        get() = shortcut.longName

    override val id: String
        get() = shortcut.cxl_id

    val category : String get() = shortcut.category

    private fun loadIcon(x:XMBItem){

    }

    private fun unloadIcon(x: XMBItem) {

    }

    private fun loadBackdrop(x: XMBItem) {

    }

    private fun unloadBackdrop(x: XMBItem) {

    }

    fun launch(x:XMBItem){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val lcher = vsh.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val uid = android.os.Process.myUid()
            var success = false
            try{
                lcher.startShortcut(shortcut.packageName, shortcut.id, vsh.xmbView?.clipBounds, null, UserHandle.getUserHandleForUid(uid))
                success = true
            }catch(_:Exception){}

            if(!success){
                vsh.postNotification(R.drawable.ic_close,
                    vsh.getString(R.string.shortcut_failed_to_launch_title),
                    vsh.getString(R.string.shortcut_failed_to_launch_desc).format(shortcut.packageName))
            }
        }
    }

    init {

    }
}