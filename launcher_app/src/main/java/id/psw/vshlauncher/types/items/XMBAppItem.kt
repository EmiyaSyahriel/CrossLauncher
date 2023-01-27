package id.psw.vshlauncher.types.items

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.INIFile
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.sequentialimages.*
import id.psw.vshlauncher.views.bootInto
import id.psw.vshlauncher.views.dialogviews.AppInfoDialogView
import id.psw.vshlauncher.views.showDialog
import java.io.File
import java.lang.StringBuilder
import android.text.format.DateFormat
import id.psw.vshlauncher.views.asBytes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
        const val ENABLE_EMBEDDED_MEDIA = false
        const val INI_KEY_TYPE = "CrossLauncher.AppInfo"
        const val INI_KEY_TITLE = "TITLE"
        const val INI_KEY_ALBUM = "ALBUM"
        const val INI_KEY_CATEGORY = "CATEGORY"
        const val INI_KEY_BOOTABLE = "BOOTABLE"
        const val INI_KEY_SUBTITLE = "SUBTITLE"

        var descriptionDisplay : DescriptionDisplay = DescriptionDisplay.PackageName

        var disableAnimatedIcon = false
        var disableBackSound = false
        var disableBackdrop = false
        var disableBackdropOverlay = false
        var showHiddenByConfig = false

        private val ios = mutableMapOf<File, Ref<Boolean>>()
        private val ioc = mutableMapOf<File, Ref<Int>>()

    }

    private var _customAppDesc: String =""
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
    private var iconFiles = ArrayList<File>().apply{
        if(!disableAnimatedIcon) {
            addAll(requestCustomizationFiles("ICON0.PNG"))
            addAll(requestCustomizationFiles("ICON0.JPG"))
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
    private var iniFile = INIFile()
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
    private var _customAppLabel = ""
    private var _appAlbum = ""
    private var _appCategory = ""

    var appCustomLabel : String
        get() = _customAppLabel
        set(value) {
            _customAppLabel = value
            writeAppConfig()
        }


    var appCustomDesc: String
        get() = _customAppDesc
        set(value) {
            _customAppDesc = value
            writeAppConfig()
        }

    var appAlbum : String
        get() = _appAlbum
        set(value) {
            _appAlbum = value
            writeAppConfig()
        }

    var appCategory : String
        get() = _appCategory
        set(value) {
            _appCategory = value
            writeAppConfig()
        }

    override val id: String get()= iconId
    override val description: String get()= displayedDescription
    override val displayName: String get()= _customAppLabel.isEmpty().select(appLabel, _customAppLabel)
    override val icon: Bitmap get()= synchronized(_icon) { _icon }
    override val backdrop: Bitmap get() = _backdrop
    override val backSound: File get() = _backSound
    override val animatedIcon: XMBFrameAnimation get() = synchronized(_animatedIcon) { _animatedIcon }
    override val hasDescription: Boolean get() = description.isNotEmpty()
    override val menuItems: ArrayList<XMBMenuItem> = arrayListOf()
    private lateinit var apkFile : File
    private lateinit var pkgInfo : PackageInfo
    private lateinit var apkSplits : Array<File>
    private lateinit var externalResource : Resources

    private val latestApkSplit : File get() {
        return if(apkSplits.size > 1){
            apkSplits.maxByOrNull { it.lastModified() } ?: apkFile
        }else{
            apkFile
        }
    }

    val sortUpdateTime get() = if(latestApkSplit.exists()) latestApkSplit.lastModified().toString() else "0"
    val displayUpdateTime : String get() {
        return if(apkFile.exists()){
            val fmt = DateFormat.is24HourFormat(vsh).select( "d/M/yyyy k:m", "d/M/yyyy h:m a")
            val sdf =
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    SimpleDateFormat(fmt, vsh.resources.configuration.locales.get(0))
                }else{
                    SimpleDateFormat(fmt, vsh.resources.configuration.locale)
                }
            sdf.format(latestApkSplit.lastModified())
        }else{
            "Unknown"
        }
    }

    val fileSize : String get() {
        var l = 0L
        if(apkSplits.size > 1){
            for(apk in apkSplits){
                l += apk.length()
            }
        }else{
            l += apkFile.length()
        }
        return l.asBytes()
    }

    val version : String get() = pkgInfo.versionName

    private val isSystemApp : Boolean get() {
        return resInfo.activityInfo.applicationInfo.flags hasFlag (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM)
    }

    private var _isHidden = false

    override val isHidden: Boolean
        get() = showHiddenByConfig.select(false, _isHidden)

    val isHiddenByCfg get()= _isHidden

    fun hide(hide: Boolean){
        _isHidden = hide
        writeAppConfig()
    }

    val packageName get() = pkgInfo.packageName

    private fun readAppConfig(){
        val files = requestCustomizationFiles("PARAM.INI")
        val validFile = files.firstOrNull { it.exists() }
        if(validFile != null){
            iniFile.parseFile(validFile.absolutePath)
        }

        _customAppLabel =   iniFile[INI_KEY_TYPE, INI_KEY_TITLE] ?: ""

        if(_customAppLabel.isEmpty()){
            forEveryLocale {
                if(_customAppLabel.isEmpty()){
                    _customAppLabel = iniFile[INI_KEY_TYPE, "$INI_KEY_TITLE-$it"] ?: ""
                }
            }
        }

        _customAppDesc  =   iniFile[INI_KEY_TYPE, INI_KEY_SUBTITLE] ?: ""

        if(_customAppDesc.isEmpty()){
            forEveryLocale {
                if(_customAppDesc.isEmpty()){
                    _customAppDesc = iniFile[INI_KEY_TYPE, "$INI_KEY_SUBTITLE-$it"] ?: ""
                }
            }
        }

        _isHidden =        (iniFile[INI_KEY_TYPE, INI_KEY_BOOTABLE] ?: "true") == "false"
        _appAlbum =         iniFile[INI_KEY_TYPE, INI_KEY_ALBUM] ?: ""
        _appCategory =      iniFile[INI_KEY_TYPE, INI_KEY_CATEGORY] ?: ""
    }

    private fun forEveryLocale(act: ((String) -> Unit)){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            val locList  = vsh.resources.configuration.locales
            val locCount = locList.size()
            for(i in 0 .. locCount){
                val loc = locList[i]
                if(loc != null){
                    act(loc.createName())
                }
            }
        }else{
            act(vsh.resources.configuration.locale.createName())
        }
    }

    private fun Locale.createName() : String {
        val sb = StringBuilder()

        val data = arrayOf(language, country, variant, "")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            data[3] = script
        }

        for(dat in data){
            if(dat.isNotEmpty()){
                if(sb.isNotEmpty()) sb.append("-")
                sb.append(dat)
            }
        }

        return sb.toString()
    }

    private fun writeAppConfig(){
        iniFile[INI_KEY_TYPE, INI_KEY_TITLE] = _customAppLabel
        iniFile[INI_KEY_TYPE, INI_KEY_SUBTITLE] = _customAppDesc
        iniFile[INI_KEY_TYPE, INI_KEY_ALBUM] = _appAlbum
        iniFile[INI_KEY_TYPE, INI_KEY_BOOTABLE] = _isHidden.select("false", "true")
        iniFile[INI_KEY_TYPE, INI_KEY_CATEGORY] = _appCategory

        if(iniFile.path.isEmpty()){
            val haveCustomFolder = vsh.getAllPathsFor(VshBaseDirs.APPS_DIR, resInfo.uniqueActivityName).any { it.exists() }
            if(!haveCustomFolder){
                createAppCustomDirectory()
            }

            val files = requestCustomizationFiles("PARAM.INI")
            val validFile = files.firstOrNull { it.exists() } ?: files[0]
            if(!validFile.exists()) validFile.createNewFile()

            iniFile.write(validFile.absolutePath)

        }else{
            iniFile.write(null)
        }
    }

    private var embeddedIconId = 0
    private var embeddedBackgroundId = 0
    private var embeddedBackOverlayId = 0
    private var embeddedBackSoundId = 0
    private var embeddedAnimIconId = 0

    init {
        readAppConfig()
        vsh.threadPool.execute {
            apkFile = File(resInfo.activityInfo.applicationInfo.publicSourceDir)

            pkgInfo = if(Build.VERSION.SDK_INT >= 33){
                vsh.packageManager.getPackageInfo(
                    resInfo.activityInfo.applicationInfo.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            }else{
                vsh.packageManager.getPackageInfo(resInfo.activityInfo.applicationInfo.packageName, 0)
            }

            if(ENABLE_EMBEDDED_MEDIA){
                externalResource = vsh.packageManager.getResourcesForApplication(pkgInfo.packageName)
                embeddedIconId = externalResource.getIdentifier("vsh_icon", "drawable", pkgInfo.packageName)
                embeddedBackgroundId = externalResource.getIdentifier("vsh_background", "drawable", pkgInfo.packageName)
                embeddedBackSoundId = externalResource.getIdentifier("vsh_background", "raw", pkgInfo.packageName)
                embeddedBackOverlayId = externalResource.getIdentifier("vsh_back_overlay", "drawable", pkgInfo.packageName)
                embeddedAnimIconId = externalResource.getIdentifier("vsh_anim_icon", "raw", pkgInfo.packageName)
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                val splits : Array<String>? = resInfo.activityInfo.applicationInfo.splitPublicSourceDirs

                apkSplits = if(splits != null){
                    if(splits.size > 1){
                        Array(splits.size){ File(splits[it]) }
                    }else{
                        Array(0){ apkFile }
                    }
                }else{
                    Array(0){ apkFile }
                }
            }

            val handle = vsh.addLoadHandle()
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            vsh.setLoadingFinished(handle)
            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_launch) }, { false }, 0){ _launch(this) })

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.menu_app_info) },
                    { false },1){
                    vsh.xmbView?.showDialog(AppInfoDialogView(vsh, this))
                }
            )

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({vsh.getString(R.string.app_create_customization_folder)}, {false}, 2){
                    createAppCustomDirectory()
                }
            )
            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_find_on_playstore) }, { false }, 3) {
                    vsh.xmbView?.context?.xmb?.appOpenInPlayStore(resInfo.activityInfo.packageName)
                }
            )

            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({vsh.getString(R.string.app_force_kill)},
                    { false }, 5)
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
                    { isSystemApp },6){
                    vsh.xmbView?.context?.xmb?.appRequestUninstall(resInfo.activityInfo.packageName)
                }
            )


            menuItems.add(
                XMBMenuItem.XMBMenuItemLambda({ vsh.getString(R.string.app_category_switch_sort) }, {false}, -2){
                    vsh.doCategorySorting()
                }
            )
        }
    }

    private fun createAppCustomDirectory() {
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

    private fun pIconLoad(){
        synchronized(_iconSync){
            if(!hasIconLoaded){
                var isCust = false
                iconFiles.find {it.exists()}?.apply {
                    try{
                        _icon = BitmapFactory.decodeFile(path)
                        isCust = true
                    }catch(e:Exception){
                        vsh.postNotification(
                            null,
                            vsh.getString(R.string.error_common_header),
                            "Icon file for package $packageName is corrupted : $path :\n${e.message}"
                        )
                    }
                }
                if(!isCust){
                    _icon = vsh.iconAdapter.create(resInfo.activityInfo, vsh)
                }
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
