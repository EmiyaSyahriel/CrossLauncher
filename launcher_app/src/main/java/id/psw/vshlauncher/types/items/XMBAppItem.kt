package id.psw.vshlauncher.types.items

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.os.Build
import id.psw.vshlauncher.*
import id.psw.vshlauncher.types.INIFile
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.sequentialimages.*
import id.psw.vshlauncher.views.bootInto
import id.psw.vshlauncher.views.dialogviews.AppInfoDialogView
import id.psw.vshlauncher.views.showDialog
import java.io.File
import java.lang.StringBuilder
import android.text.format.DateFormat
import id.psw.vshlauncher.types.CIFLoader
import id.psw.vshlauncher.types.FileQuery
import id.psw.vshlauncher.views.asBytes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class XMBAppItem(private val vsh: VSH, val resInfo : ResolveInfo) : XMBItem(vsh) {
    enum class DescriptionDisplay(val v : Int) {
        /** No description is displayed */
        None(0),
        /** Default - Android App's package name */
        PackageName(1),
        /** Update Date Time */
        Date(2),
        /** Size of APK and Split APK Sum (not including app data size) */
        FileSize(3),
        /** CrossLauncher Modification ID */
        ModificationId(4),
        /** App version */
        Version(5),
        /** Nokia S40 List Style file description (Date      Size) */
        NkFileStyle(40)
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

        var showHiddenByConfig = false

        var sdf : SimpleDateFormat? = null
        const val loadingText = "[...]"
        private val SDF
        get() = sdf ?: SimpleDateFormat.getDateInstance()
    }

    private var _customAppDesc: String =""
    private var _icon = TRANSPARENT_BITMAP

    private val iconId : String = "${resInfo.activityInfo.processName}::${resInfo.activityInfo.packageName}"
    private var appLabel = ""
    private val displayedDescription : String get(){
        return when (descriptionDisplay){
            DescriptionDisplay.Date -> SDF.format(apkFile?.lastModified() ?: 1)
            DescriptionDisplay.PackageName -> resInfo.activityInfo.processName
            DescriptionDisplay.ModificationId -> resInfo.uniqueActivityName
            DescriptionDisplay.FileSize -> fileSize
            DescriptionDisplay.NkFileStyle -> "$fileSize     ${SDF.format(apkFile?.lastModified() ?: 1)}"
            DescriptionDisplay.None -> ""
            else -> ""
        }
    }

    private val cif = CIFLoader(vsh, resInfo, FileQuery(VshBaseDirs.APPS_DIR).withNames(resInfo.uniqueActivityName).execute(vsh))

    override val isIconLoaded: Boolean get()= cif.hasIconLoaded
    override val isAnimatedIconLoaded: Boolean get() = cif.hasAnimIconLoaded
    override val isBackSoundLoaded: Boolean get() = cif.hasBackSoundLoaded
    override val isBackdropLoaded: Boolean get() = cif.hasBackdropLoaded
    override val isPortraitBackdropLoaded: Boolean get() = cif.hasPortBackdropLoaded

    override val hasIcon: Boolean get()= true
    override val hasBackdrop: Boolean get() = cif.hasBackdrop
    override val hasPortraitBackdrop: Boolean get() = cif.hasPortraitBackdrop
    override val hasBackOverlay: Boolean get() = cif.hasBackOverlay
    override val hasPortraitBackdropOverlay: Boolean get() = cif.hasPortraitBackdropOverlay
    override val hasBackSound: Boolean get() = cif.hasBackSound
    override val hasAnimatedIcon: Boolean get() = cif.hasAnimatedIcon
    private var iniFile = INIFile()

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
    override val icon: Bitmap get()= cif.icon.bitmap
    override val backdrop: Bitmap get() = cif.backdrop.bitmap
    override val backSound: File get() = cif.backSound
    override val animatedIcon: XMBFrameAnimation get() = synchronized(cif.animIcon) { cif.animIcon }
    override val hasDescription: Boolean get() = description.isNotEmpty()
    override val menuItems: ArrayList<XMBMenuItem> = arrayListOf()
    private var apkFile : File? = null
    private var pkgInfo : PackageInfo? = null
    private var apkSplits : Array<File> = arrayOf()
    private var externalResource : Resources? = null

    private val latestApkSplit : File? get() {
        return if(apkSplits.size > 1){
            apkSplits.maxByOrNull { it.lastModified() } ?: apkFile
        }else{
            apkFile
        }
    }

    val sortUpdateTime get() =
        if(latestApkSplit?.exists() == true) latestApkSplit?.lastModified().toString() else "0"
    val displayUpdateTime : String get() {
        return if(apkFile?.exists() == true){
            val fmt = DateFormat.is24HourFormat(vsh).select( "d/M/yyyy k:m", "d/M/yyyy h:m a")
            val sdf =
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    SimpleDateFormat(fmt, vsh.resources.configuration.locales.get(0))
                }else{
                    SimpleDateFormat(fmt, vsh.resources.configuration.locale)
                }
            sdf.format(latestApkSplit?.lastModified())
        }else{
            "Unknown"
        }
    }

    val fileSize : String get() {
        return if(apkSplits.size > 1){
            var l = 0L
            for(apk in apkSplits){
                l += apk.length()
            }
            (l > 0L).select(l.asBytes(), loadingText)
        }else{
            apkFile?.length()?.asBytes() ?: loadingText
        }
    }

    val version : String get() = pkgInfo?.versionName ?: loadingText

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

    val packageName get() = pkgInfo?.packageName ?: loadingText

    private fun readAppConfig(){
        val files = cif.requestCustomizationFiles("PARAM", VshResTypes.INI)
        val validFile = files.firstOrNull { it.exists() }
        if(validFile != null){
            iniFile.parseFile(validFile.absolutePath)
        }

        _customAppLabel =  iniFile[INI_KEY_TYPE, INI_KEY_TITLE] ?: ""

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
        iniFile[XMBAppItem.INI_KEY_TYPE, XMBAppItem.INI_KEY_TITLE] = appCustomLabel
        iniFile[XMBAppItem.INI_KEY_TYPE, XMBAppItem.INI_KEY_SUBTITLE] = appCustomDesc
        iniFile[XMBAppItem.INI_KEY_TYPE, XMBAppItem.INI_KEY_ALBUM] = appAlbum
        iniFile[XMBAppItem.INI_KEY_TYPE, XMBAppItem.INI_KEY_BOOTABLE] = isHidden.select("false", "true")
        iniFile[XMBAppItem.INI_KEY_TYPE, XMBAppItem.INI_KEY_CATEGORY] = appCategory

        if(iniFile.path.isEmpty()){
            vsh.tryMigrateOldGameDirectory()
            val haveCustomFolder = FileQuery(VshBaseDirs.APPS_DIR).atPath(resInfo.uniqueActivityName).createParentDirectory(true).execute(vsh).any { it.exists() }
            if(!haveCustomFolder){
                createAppCustomDirectory()
            }

            val files = cif.requestCustomizationFiles("PARAM", VshResTypes.INI)
            var success = false
            files.forEach { file ->
                try {
                    if(!file.exists()) file.createNewFile()

                    iniFile.write(file.absolutePath)
                    success = true
                    return@forEach
                }catch(_:Exception ){
                }
            }
            if(!success){
                vsh.postNotification(R.drawable.ic_close, "Error", "Failed to write application-specific configuration")
            }

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

            loadEmbeddedResource()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                val splits : Array<String>? = resInfo.activityInfo.applicationInfo.splitPublicSourceDirs

                apkSplits = if(splits != null){
                    if(splits.size > 1){
                        Array(splits.size){ File(splits[it]) }
                    }else{
                        Array(0){ apkFile!!}
                    }
                }else{
                    Array(0){ apkFile!! }
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

    @SuppressLint("DiscouragedApi")
    private fun loadEmbeddedResource() {
        if(ENABLE_EMBEDDED_MEDIA){
            val pkg = pkgInfo
            val res = externalResource
            if(pkg != null && res != null){
                externalResource = vsh.packageManager.getResourcesForApplication(pkg.packageName)
                embeddedIconId = res.getIdentifier("vsh_icon", "drawable", pkg.packageName)
                embeddedBackgroundId = res.getIdentifier("vsh_background", "drawable", pkg.packageName)
                embeddedBackSoundId = res.getIdentifier("vsh_background", "raw", pkg.packageName)
                embeddedBackOverlayId = res.getIdentifier("vsh_back_overlay", "drawable", pkg.packageName)
                embeddedAnimIconId = res.getIdentifier("vsh_anim_icon", "raw", pkg.packageName)
            }
        }
    }

    private fun createAppCustomDirectory() {
        vsh.tryMigrateOldGameDirectory()
        FileQuery(VshBaseDirs.APPS_DIR).atPath(resInfo.uniqueActivityName).createParentDirectory(true){file, created ->
            val sb = StringBuilder()
            for(i in file.absolutePath.indices){
                if((i + 1) % 50 == 0) sb.append('\n')
                sb.append(file.absolutePath[i])
            }

            if(created){
                vsh.postNotification(null, vsh.getString(R.string.app_customization_file_created), sb.toString(), 10.0f)
            }
        }.execute(vsh)
    }

    private fun pIconLoad(){
        cif.loadIcon()
    }

    private fun pIconUnload(){
        cif.unloadIcon()
    }

    private fun pBackdropLoad(){
        cif.loadBackdrop()
    }

    private fun pBackdropUnload(){
        cif.unloadBackdrop()
    }

    private fun pSoundLoad(){
        cif.loadSound()
    }

    private fun pSoundUnload(){
        cif.unloadSound()
    }

    private fun pOnScreenVisible(i : XMBItem){
        vsh.threadPool.execute {
            appLabel = resInfo.loadLabel(vsh.packageManager).toString()
            if(_icon == TRANSPARENT_BITMAP){
                cif.loadIcon()
            }
        }
    }

    private fun pOnScreenInvisible(i : XMBItem){
        // Destroy icon, Unload it from memory
        vsh.threadPool.execute {
            if(vsh.aggressiveUnloading){
                if(_icon != TRANSPARENT_BITMAP){
                    cif.unloadIcon()
                }
            }
        }
    }

    private fun pOnHovered(i : XMBItem){
        vsh.threadPool.execute {
            cif.loadBackdrop()
            cif.loadSound()
        }
    }

    private fun pOnUnHovered(i: XMBItem){
        vsh.threadPool.execute {
            cif.unloadBackdrop()
            cif.unloadSound()
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
