package id.psw.vshlauncher.types.items

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.sequentialimages.*
import id.psw.vshlauncher.views.bootInto
import java.io.File
import java.lang.StringBuilder

class XMBAppItem(private val vsh: VSH, val resInfo : ResolveInfo) : XMBItem(vsh) {
    enum class DescriptionDisplay {
        None,
        PackageName,
        Date,
        FileSize,
        ModificationId
    }

    companion object {
        private const val TAG = "XMBAppItem"
        var descriptionDisplay : DescriptionDisplay = DescriptionDisplay.PackageName

        var disableAnimatedIcon = false
        var disableBackSound = false
        var disableBackdrop = false
        var disableBackdropOverlay = false

        private val ios = mutableMapOf<File, Ref<Boolean>>()
        private val ioc = mutableMapOf<File, Ref<Int>>()

    }

    private var _icon = TRANSPARENT_BITMAP

    private var hasIconLoaded = false
    private var hasAnimIconLoaded = false
    private var hasBackdropLoaded = false
    private var hasPortBackdropLoaded = false
    private var hasBackSoundLoaded = false

    private val iconId : String = "${resInfo.activityInfo.processName}::${resInfo.activityInfo.packageName}"
    private var appLabel = ""
    private var _animatedIcon : XMBFrameAnimation = TRANSPARENT_ANIM_BITMAP
    private var _backdrop = TRANSPARENT_BITMAP
    private var _backOverlay = TRANSPARENT_BITMAP
    private var _portBackdrop = TRANSPARENT_BITMAP
    private var _portBackdropOverlay = TRANSPARENT_BITMAP
    private var _backSound : File = SILENT_AUDIO
    private val displayedDescription : String get(){
        return when (descriptionDisplay){
            DescriptionDisplay.Date -> apkFile.lastModified().toString()
            DescriptionDisplay.PackageName -> resInfo.activityInfo.processName
            DescriptionDisplay.ModificationId -> resInfo.uniqueActivityName
            DescriptionDisplay.FileSize -> apkFile.length().toString()
            DescriptionDisplay.None -> ""
            else -> ""
        }
    }

    private fun requestCustomizationFiles(fileName:String) : ArrayList<File>{
        return vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName, fileName, createParentDir = false)
    }

    private var backdropFiles = ArrayList<File>().apply {
        if(!disableBackdrop) {
            addAll(requestCustomizationFiles("PIC1.PNG"))
            addAll(requestCustomizationFiles("PIC1.JPG"))
        }
    }
    private var backdropOverlayFiles = ArrayList<File>().apply {
        if(!disableBackdropOverlay) {
            addAll(requestCustomizationFiles("PIC0.PNG"))
            addAll(requestCustomizationFiles("PIC0.JPG"))
        }
    }
    private var portraitBackdropFiles = ArrayList<File>().apply {
        if(!disableBackdrop) {
            addAll(requestCustomizationFiles("PIC1_P.PNG"))
            addAll(requestCustomizationFiles("PIC1_P.JPG"))
        }
    }
    private var portraitBackdropOverlayFiles = ArrayList<File>().apply {
        if(!disableBackdropOverlay) {
            addAll(requestCustomizationFiles("PIC0_P.PNG"))
            addAll(requestCustomizationFiles("PIC0_P.JPG"))
        }
    }
    private var animatedIconFiles = ArrayList<File>().apply{
        if(!disableAnimatedIcon) {
            addAll(requestCustomizationFiles("ICON1.APNG")) // Animated PNG (Line APNG-Drawable), best quality, just bigger file, Renderer OK
            addAll(requestCustomizationFiles("ICON1.WEBP")) // WEBP (Facebook Fresco), good quality, relatively small file, Renderer Bad
            addAll(requestCustomizationFiles("ICON1.MP4")) // MP4 (Android MediaMetadataRetriever), average quality, small file, Renderer Slow
            addAll(requestCustomizationFiles("ICON1.GIF")) // GIF (Facebook Fresco), low quality, small file, Renderer Bad
        }
    }
    private var backSoundFiles = ArrayList<File>().apply {
        if(!disableBackSound) {
            addAll(requestCustomizationFiles("SND0.MP3"))
            addAll(requestCustomizationFiles("SND0.AAC"))
            addAll(requestCustomizationFiles("SND0.MID"))
            addAll(requestCustomizationFiles("SND0.MIDI"))
        }
    }
    private var _iconSync = Object()
    private var _animIconSync = Object()
    private var _backdropSync = Object()
    private var _portBackdropSync = Object()
    private var _backSoundSync = Object()
    private fun <K> MutableMap<File, Ref<K>>.getOrMake(k:File, refDefVal:K) = getOrMake<File, Ref<K>>(k){ Ref<K>(refDefVal) }

    override val isIconLoaded: Boolean get()= hasIconLoaded
    override val isAnimatedIconLoaded: Boolean get() = hasAnimIconLoaded
    override val isBackSoundLoaded: Boolean get() = hasBackSoundLoaded
    override val isBackdropLoaded: Boolean get() = hasBackdropLoaded
    override val isPortraitBackdropLoaded: Boolean get() = hasPortBackdropLoaded

    override val hasIcon: Boolean get()= true
    override val hasBackdrop: Boolean get() = !disableBackdrop &&
            backdropFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasPortraitBackdrop: Boolean get() = !disableBackdrop &&
            portraitBackdropFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasBackOverlay: Boolean get() = !disableBackdropOverlay &&
            backdropOverlayFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasPortraitBackdropOverlay: Boolean get() = !disableBackdropOverlay &&
            portraitBackdropOverlayFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasBackSound: Boolean get() = !disableBackSound &&
            backSoundFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasAnimatedIcon: Boolean get() = !disableAnimatedIcon &&
            animatedIconFiles.any {
                it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false))
            }
    override val hasMenu: Boolean get() = true

    override val id: String get()= iconId
    override val description: String get()= displayedDescription
    override val displayName: String get()= appLabel
    override val icon: Bitmap get()= synchronized(_icon) { _icon }
    override val backdrop: Bitmap get() = _backdrop
    override val backSound: File get() = _backSound
    override val animatedIcon: XMBFrameAnimation get() = synchronized(_animatedIcon) { _animatedIcon }
    override val hasDescription: Boolean get() = description.isNotEmpty()
    override val menuItems: ArrayList<XMBMenuItem> = arrayListOf()
    private lateinit var apkFile : File

    val sortUpdateTime get() = if(apkFile.exists()) apkFile.lastModified().toString() else "0"
    val fileSize get() = if(apkFile.exists()) apkFile.length().toString() else "0"

    private val isSystemApp : Boolean get() {
        return resInfo.activityInfo.applicationInfo.flags hasFlag (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM)
    }

    init {
        vsh.threadPool.execute {
            apkFile = File(resInfo.activityInfo.applicationInfo.publicSourceDir)
            val handle = vsh.addLoadHandle()
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            vsh.setLoadingFinished(handle)
            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_launch) }, { false }, 0){ _launch(this) })


            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({vsh.getString(R.string.app_force_kill)},
                    { false }, 1)
                {
                    val actMan = vsh.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    actMan.killBackgroundProcesses(resInfo.activityInfo.processName)
                    vsh.postNotification(null,
                        vsh.getString(R.string.force_kill_sent_title),
                        vsh.getString(R.string.force_kill_sent_desc, resInfo.activityInfo.processName))
                }
            )

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_uninstall) },
                    { isSystemApp },2){
                    vsh.xmbView?.context?.xmb?.appRequestUninstall(resInfo.activityInfo.packageName)
                }
            )


            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_find_on_playstore) }, { false }, 4) {
                    vsh.xmbView?.context?.xmb?.appOpenInPlayStore(resInfo.activityInfo.packageName)
                }
            )

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({vsh.getString(R.string.app_create_customization_folder)}, {false}, 5){
                    vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName).forEach { file ->
                        var found = file.exists()
                        if(!found){
                            found = file.mkdirs()
                        }
                        val sb = StringBuilder()
                        for(i in file.absolutePath.indices){
                            if((i + 1) % 50 == 0) sb.append('\n')
                            sb.append(file.absolutePath[i])
                        }

                        if(found){
                            vsh.postNotification(null, vsh.getString(R.string.app_customization_file_created), sb.toString(), 10.0f)
                        }
                    }
                }
            )

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_category_switch_sort) }, {false}, -2){
                    vsh.doCategorySorting()
                }
            )
        }
    }

    private fun pIconLoad(){
        synchronized(_iconSync){
            if(!hasIconLoaded){
                _icon = vsh.iconAdapter.create(resInfo.activityInfo, vsh)
                hasIconLoaded = true
            }
        }
        if(vsh.playAnimatedIcon){
            synchronized(_animIconSync){
                if((!hasAnimIconLoaded || _animatedIcon.hasRecycled)){
                    animatedIconFiles.find { it.exists() }?.apply{
                        _animatedIcon = when (this.extension.uppercase()) {
                            "WEBP" -> XMBAnimWebP(this)
                            "APNG" -> XMBAnimAPNG(this)
                            "MP4" -> XMBAnimMMR(this.absolutePath)
                            "GIF" -> XMBAnimGIF(this)
                            else -> WHITE_ANIM_BITMAP
                        }
                        hasAnimIconLoaded = true
                    }
                }
            }
        }
    }

    private fun pBackdropLoad(){
        synchronized(_backdropSync){
            if(!hasBackdropLoaded){
                backdropFiles.find{
                    it.exists()
                }?.apply {
                    _backdrop = BitmapFactory.decodeFile(this.absolutePath)
                    hasBackdropLoaded = true
                }
            }
        }
    }

    private fun pBackdropUnload(){
        synchronized(_backdropSync){
            if(hasBackdropLoaded){
                hasBackdropLoaded = false
                if(_backdrop != TRANSPARENT_BITMAP) _backdrop.recycle()
                _backdrop = TRANSPARENT_BITMAP
            }
        }
    }

    private fun pSoundLoad(){
        synchronized(_backSoundSync){
            backSoundFiles.find { it.exists() }?.let {
                _backSound = it
                hasBackSoundLoaded = true
            }
        }
    }

    private fun pSoundUnload(){
        synchronized(_backSoundSync){
            if(hasBackSoundLoaded) {
                hasBackSoundLoaded = false
                _backSound = SILENT_AUDIO
            }
        }
    }

    private fun pIconUnload(){
        synchronized(_iconSync) {
            if(hasIconLoaded){
                hasIconLoaded = false
                if(_icon != TRANSPARENT_BITMAP) _icon.recycle()
                _icon = TRANSPARENT_BITMAP
            }
        }
        synchronized(_animIconSync){
            if(hasAnimIconLoaded || !_animatedIcon.hasRecycled){
                hasAnimIconLoaded = false
                if(_animatedIcon != WHITE_ANIM_BITMAP) _animatedIcon.recycle()
            }
        }
    }

    private fun pOnScreenVisible(i : XMBItem){
        vsh.threadPool.execute {
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            if(_icon == TRANSPARENT_BITMAP){
                pIconLoad()
            }
        }
    }

    private fun pOnScreenInvisible(i : XMBItem){
        // Destroy icon, Unload it from memory
        vsh.threadPool.execute {
            if(vsh.aggressiveUnloading){
                if(_icon != TRANSPARENT_BITMAP){
                    pIconUnload()
                }
            }
        }
    }

    private fun pOnHovered(i : XMBItem){
        vsh.threadPool.execute {
            pBackdropLoad()
            pSoundLoad()
        }
    }

    private fun pOnUnHovered(i: XMBItem){
        vsh.threadPool.execute {
            pBackdropUnload()
            pSoundUnload()
        }
    }

    override val onScreenVisible: (XMBItem) -> Unit get()= ::pOnScreenVisible
    override val onScreenInvisible: (XMBItem) -> Unit get()= ::pOnScreenInvisible
    override val onHovered: (XMBItem) -> Unit get() = ::pOnHovered
    override val onUnHovered: (XMBItem) -> Unit get() = ::pOnUnHovered

    private fun _launch(i: XMBItem){
        vsh.xmbView?.bootInto(false){
            try{
                val launchInfo = vsh.packageManager.getLaunchIntentForPackage(resInfo.activityInfo.packageName)
                vsh.startActivity(launchInfo)
                vsh.preventPlayMedia = true
            }catch(e:Exception){
                vsh.postNotification(null, "Launch failed","Unable to launch this app, most likely due to this app is not available on the device", 10.0f)
            }
        }
    }

    override val onLaunch: (XMBItem) -> Unit get()= ::_launch
}