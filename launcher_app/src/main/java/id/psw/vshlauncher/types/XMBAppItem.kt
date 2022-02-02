package id.psw.vshlauncher.types

import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.views.bootInto
import java.io.File

class XMBAppItem(private val vsh: VSH, val resInfo : ResolveInfo) : XMBItem(vsh) {
    private var _icon = TRANSPARENT_BITMAP
    private var hasIconLoaded = false
    private val iconId : String = "${resInfo.activityInfo.processName}::${resInfo.activityInfo.packageName}"
    private var appLabel = ""
    private var displayedDescription = "Please check launcher setting"
    private var packageName = resInfo.activityInfo.name
    private var processName = resInfo.activityInfo.processName
    private var backdropFiles = vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName, "PIC0.PNG", createParentDir = true)
    private var animatedIconFiles = vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName, "ICON1.WEBP", createParentDir = true)
    private var backSoundFiles = vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName, "SND0.AAC", createParentDir = true)

    override val isIconLoaded: Boolean get()= !hasIconLoaded
    override val id: String get()= iconId
    override val description: String get()= displayedDescription
    override val displayName: String get()= appLabel
    override val hasIcon: Boolean get()= true
    override val icon: Bitmap get()= _icon
    override val hasBackdrop: Boolean get() = backdropFiles.any { it.exists() }
    override val hasBackSound: Boolean get() = backSoundFiles.any { it.exists() }
    override val hasAnimatedIcon: Boolean get() = animatedIconFiles.any { it.exists() }

    init{
        vsh.threadPool.execute {
            val handle = vsh.addLoadHandle()
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            _iconLoad()
            vsh.setLoadingFinished(handle)
        }
    }

    private fun _iconLoad(){
        _icon = VSH.IconAdapter.create(resInfo.activityInfo)
        hasIconLoaded = true
    }

    private fun _iconUnload(){
        synchronized(_icon) {
            hasIconLoaded = false
            _icon.recycle()
            _icon = TRANSPARENT_BITMAP
        }
    }

    private fun _onScreenVisible(i : XMBItem){
        vsh.threadPool.execute {
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            if(_icon == TRANSPARENT_BITMAP){
                _iconLoad()
            }
        }
    }

    private fun _onScreenInvisible(i : XMBItem){
        // Destroy icon, Unload it from memory
        vsh.threadPool.execute {
            appLabel = "--UNLOADED--"
            if(vsh.aggressiveUnloading){
                if(_icon != TRANSPARENT_BITMAP){
                    _iconUnload()
                }
            }
        }
    }

    override val onScreenVisible: (XMBItem) -> Unit get()= ::_onScreenVisible
    override val onScreenInvisible: (XMBItem) -> Unit get()= ::_onScreenInvisible

    private fun _launch(i: XMBItem){
        vsh.vshView?.bootInto(false){
            val launchInfo = vsh.packageManager.getLaunchIntentForPackage(resInfo.activityInfo.packageName)
            vsh.startActivity(launchInfo)
        }
    }

    override val onLaunch: (XMBItem) -> Unit get()= ::_launch
}