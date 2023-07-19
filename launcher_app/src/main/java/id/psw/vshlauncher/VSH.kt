package id.psw.vshlauncher

import android.app.ActivityManager
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import id.psw.vshlauncher.livewallpaper.NativeGL
// import com.facebook.drawee.backends.pipeline.Fresco
import id.psw.vshlauncher.submodules.*
import id.psw.vshlauncher.types.*
import id.psw.vshlauncher.types.Stack
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.types.items.XMBItemCategory
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.filterBySearch
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class VSH : Application() {

    companion object {
        private lateinit var _appFont : Typeface
        val AppFont get() = _appFont
        const val TAG = "VshApp"
        const val ITEM_CATEGORY_HOME = "vsh_home"
        const val ITEM_CATEGORY_APPS = "vsh_apps"
        const val ITEM_CATEGORY_GAME = "vsh_game"
        const val ITEM_CATEGORY_VIDEO = "vsh_video"
        const val ITEM_CATEGORY_SHORTCUT = "vsh_shortcut"
        const val ITEM_CATEGORY_MUSIC = "vsh_music"
        const val ITEM_CATEGORY_SETTINGS = "vsh_settings"
        const val COPY_DATA_SIZE_BUFFER = 10240
        const val ACT_REQ_INSTALL_PACKAGE = 4496

        val dbgMemInfo = Debug.MemoryInfo()
        val actMemInfo = ActivityManager.MemoryInfo()
    }

    lateinit var _gamepad : GamepadSubmodule
    var showDebuggerCount = 0
    var _gamepadUi : GamepadUISubmodule = GamepadUISubmodule()
    lateinit var pluginManager : PluginManager
    lateinit var iconAdapter : XMBAdaptiveIconRenderer
    lateinit var network : NetworkSubmodule

    private var appUserUid = 0
    /** Used as user's identifier when using an external storage */
    val UserUid get() = appUserUid

    val selectStack = Stack<String>()
    var aggressiveUnloading = true
    val runtimeTriageList = ArrayList<String>()
    var xmbView : XmbView? = null
    var playAnimatedIcon = true
    var mediaListingStarted = false

    val categories = arrayListOf<XMBItemCategory>()
    /** Return all item in current selected category or current active item, including the hidden ones */
    val items : ArrayList<XMBItem>? get(){
        try {
            var root = categories.visibleItems.find { it.id == selectedCategoryId }?.content
            var i = 0
            while(root != null && i < selectStack.size){
                root = root.find { it.id == selectStack[i]}?.content
                i++
            }
            if(root != null){
                if(root.indexOfFirst { it.id == selectedItemId } < 0 && root.size > 0){
                    selectedItemId = root[0].id
                }
            }
            return root
        }catch(e:Exception){
            e.printStackTrace()
            vsh.postNotification(R.drawable.ic_error, e.javaClass.name, e.toString())
        }
        return arrayListOf<XMBItem>()
    }

    val activeParent : XMBItem? get(){
        var root = categories.visibleItems.find { it.id == selectedCategoryId }
        var i = 0
        while(root != null && i < selectStack.size){
            root = root.content?.find { it.id == selectStack[i]}
            i++
        }
        return root
    }

    val hoveredItem : XMBItem? get() = items?.find { it.id == selectedItemId }
    private var _waveShouldRefresh = false
    var showLauncherFPS = true
    var waveShouldReReadPreferences : Boolean get() {
        val r = _waveShouldRefresh
        _waveShouldRefresh = false
        return r
    }

    set(v) { _waveShouldRefresh = v }

    var selectedCategoryId = ITEM_CATEGORY_APPS
    var selectedItemId = ""
    lateinit var pref : SharedPreferences

    val itemCursorX get() = categories.visibleItems.indexOfFirst { it.id == selectedCategoryId }
    val itemCursorY get() = (items?.visibleItems?.filterBySearch(this)?.indexOfFirst { it.id == selectedItemId } ?: -1).coerceAtLeast(0)
    var itemOffsetX = 0.0f
    var itemOffsetY = 0.0f
    var itemBackdropAlphaTime = 0.0f

    val isInRoot : Boolean get() = selectStack.size == 0
    var preventPlayMedia = true
    var notificationLastCheckTime = 0L
    val notifications = arrayListOf<XMBNotification>()
    val threadPool: ExecutorService = Executors.newFixedThreadPool(8)
    val loadingHandles = arrayListOf<XMBLoadingHandle>()
    val hiddenCategories = arrayListOf(ITEM_CATEGORY_MUSIC, ITEM_CATEGORY_VIDEO)

    val volume = VolumeManager()
    val bgmPlayer = MediaPlayer()
    val systemBgmPlayer = MediaPlayer()
    lateinit var bgmPlayerActiveSrc : File
    var bgmPlayerDoNotAutoPlay = false
    lateinit var sfxPlayer : SoundPool
    val sfxIds = mutableMapOf<SFXType, Int>()
    var shouldShowExitOption = false

    var useInternalWave = true

    fun reloadPreference() {
        pref = getSharedPreferences("xRegistry.sys", Context.MODE_PRIVATE)
        volume.pref = pref
        setActiveLocale(readSerializedLocale(pref.getString(PrefEntry.SYSTEM_LANGUAGE, "") ?: ""))
        showLauncherFPS = pref.getInt(PrefEntry.SHOW_LAUNCHER_FPS, 0) == 1
        CIFLoader.disableAnimatedIcon = pref.getInt(PrefEntry.DISABLE_VIDEO_ICON, 0) != 0
        val o = pref.getInt(PrefEntry.SYSTEM_VISIBLE_APP_DESC, XMBAppItem.DescriptionDisplay.PackageName.ordinal)
        XMBAppItem.descriptionDisplay = enumFromInt(o)
        XMBAdaptiveIconRenderer.Companion.AdaptiveRenderSetting.iconPriority =
            vsh.pref.getInt(PrefEntry.ICON_RENDERER_PRIORITY, 0b01111000)
    }

    private fun updateVolume(channel: VolumeManager.Channel, vol : Float){
        Logger.d(TAG, "VolChange :: $channel @ $vol")
        when(channel){
            VolumeManager.Channel.Sfx -> {
                sfxIds.forEach { i ->
                    sfxPlayer.setVolume(i.value, vol, vol)
                }

            }
            VolumeManager.Channel.Bgm -> {
                bgmPlayer.setVolume(vol, vol)
            }
            VolumeManager.Channel.SystemBgm -> {
                systemBgmPlayer.setVolume(vol, vol)
            }
            else -> {
                updateVolume(VolumeManager.Channel.Sfx, volume.sfx * volume.master)
                updateVolume(VolumeManager.Channel.Bgm, volume.bgm * volume.master)
                updateVolume(VolumeManager.Channel.SystemBgm, volume.systemBgm * volume.master)
            }
        }
    }

    override fun onCreate() {
        Logger.init(this)
        BitmapManager.instance = BitmapManager().apply { init(vsh) }
        pluginManager = PluginManager(this)
        pluginManager.reloadPluginList()
        reloadPreference()
        if(sdkAtLeast(21)){
            val attr =AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_MEDIA)

            if(sdkAtLeast(29)){
                attr.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
            }

            sfxPlayer = SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attr.build())
                .build()
        }else{
            sfxPlayer = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }
        volume.onVolumeChange = {a,b -> updateVolume(a, b)}
        volume.readPreferences()
        FontCollections.init(this)
        preparePlaceholderAudio()
        bgmPlayer.setOnPreparedListener {
            it.isLooping = true
            if(!bgmPlayerDoNotAutoPlay) it.start()
        }
        systemBgmPlayer.setOnPreparedListener {
            it.isLooping = false
            it.start()
        }

        // Fresco.initialize(this)
        notificationLastCheckTime = SystemClock.uptimeMillis()
        _gamepad = GamepadSubmodule(this)
        iconAdapter = XMBAdaptiveIconRenderer(this)
        network = NetworkSubmodule(this)
        registerInternalCategory()
        reloadAppList()
        reloadShortcutList()
        fillSettingsCategory()
        loadSfxData()
        addHomeScreen()
        installBroadcastReceivers()

        super.onCreate()
    }

    private fun installBroadcastReceivers() {
        // Package Install / uninstall
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                postNotification(null, "Updating database...","Device package list has been changed, updating list...")
                reloadAppList()
            }
        }, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        })
    }

    private var shouldExit = false
    var doMemoryInfoGrab = false
    private lateinit var meminfoThread : Thread
    private fun memoryInfoReaderFunc(){
        /* Disable memory usage reading :
        // Most Android phone would spams "memtrack module not found" or something like that
        val actman = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        while(!shouldExit){
            if(doMemoryInfoGrab){
                Debug.getMemoryInfo(dbgMemInfo)
                actman.getMemoryInfo(actMemInfo)
            }
            Thread.sleep(1000)
        }*/
    }

    private fun listLogAllSupportedLocale() {
        val sb = StringBuilder()
        sb.appendLine("Listed supported languages / locales in this app: ")
        supportedLocaleList.forEach {
            sb.appendLine("- ${getStringLocale(it, R.string.category_games)}")
        }
        Logger.d(TAG, sb.toString())
    }

    fun moveCursorX(right:Boolean){
        val items = categories.visibleItems
        if(items.isNotEmpty()){
            preventPlayMedia = false
            var cIdx = items.indexOfFirst { it.id == selectedCategoryId }
            val oldIdx = cIdx
            items[cIdx].lastSelectedItemId = selectedItemId // Save last category item id
            items[cIdx].content?.forEach { it.isHovered = false }
            if(right) cIdx++ else cIdx--
            cIdx = cIdx.coerceIn(0, items.size - 1)
            if(cIdx != oldIdx) {
                itemOffsetX = right.select(1.0f, -1.0f)
                itemBackdropAlphaTime = 0.0f
            }
            selectedItemId = items[cIdx].lastSelectedItemId // Load new category item id
            items[cIdx].content?.forEach { it.isHovered = it.id == selectedItemId }
            selectedCategoryId = items[cIdx].id
            xmbView?.state?.itemMenu?.selectedIndex = 0
            vsh.playSfx(SFXType.Selection)

            xmbView?.state?.crossMenu?.verticalMenu?.nameTextXOffset = 0.0f
            xmbView?.state?.crossMenu?.verticalMenu?.descTextXOffset = 0.0f
        }
    }

    fun queryTexture(customId:String) : ArrayList<File> =FileQuery(VshBaseDirs.VSH_RESOURCES_DIR).withNames("$customId.png").execute(this)

    fun loadTexture(@DrawableRes d : Int, w:Int, h:Int, whiteFallback:Boolean = false ) : Bitmap {
        return ResourcesCompat.getDrawable(resources, d, null)?.toBitmap(w, h) ?: whiteFallback.select(XMBItem.WHITE_BITMAP, XMBItem.TRANSPARENT_BITMAP)
    }
    fun loadTexture(@DrawableRes d : Int, whiteFallback:Boolean = false ) : Bitmap {
        val dwb =ResourcesCompat.getDrawable(resources, d, null)
        return dwb?.toBitmap(dwb.intrinsicWidth ?: 1, dwb.intrinsicHeight?: 1) ?: whiteFallback.select(XMBItem.WHITE_BITMAP, XMBItem.TRANSPARENT_BITMAP)
    }
    fun loadTexture(@DrawableRes d: Int, customId:String, w:Int, h:Int, whiteFallback: Boolean) : Bitmap{
        var retval : Bitmap? = null
        val file = queryTexture(customId).find { it.exists() }
        if(file != null){
            try {
                retval = BitmapFactory.decodeFile(file.absolutePath)
                val dr = retval.scale(w, h)
                retval.recycle()
                retval = dr
            }catch(e:Exception){
                postNotification(R.drawable.ic_error, e.javaClass.name, "Failed to decode file ${file.absolutePath} : ${e.message}")
            }
        }

        return retval ?: loadTexture(d, w, h, whiteFallback)
    }
    fun loadTexture(@DrawableRes d: Int, customId:String, whiteFallback: Boolean) : Bitmap{
        var retval : Bitmap? = null
        val file = queryTexture(customId).find { it.exists() }
        if(file != null){
            try {
                retval = BitmapFactory.decodeFile(file.absolutePath)
            }catch(e:Exception){
                postNotification(R.drawable.ic_error, e.javaClass.name, "Failed to decode file ${file.absolutePath} : ${e.message}")
            }
        }

        return retval ?: loadTexture(d, whiteFallback)
    }

    fun moveCursorY(bottom:Boolean){
        try{
            val items = items?.visibleItems?.filterBySearch(this)
            if(items != null){
                preventPlayMedia = false
                if(items.isNotEmpty()){
                    var cIdx = items.indexOfFirst { it.id == selectedItemId }

                    if(cIdx == -1 && items.isNotEmpty()) cIdx = 0

                    val oldIdx = cIdx
                    if(bottom) cIdx++ else cIdx--
                    if(cIdx < 0) cIdx = items.size - 1
                    if(cIdx >= items.size ) cIdx = 0

                    cIdx  = cIdx.coerceIn(0, items.size - 1)
                    if(cIdx != oldIdx) {
                        itemOffsetY = bottom.select(1.0f, -1.0f)
                        itemBackdropAlphaTime = 0.0f
                    }
                    selectedItemId = items[cIdx].id
                }

                xmbView?.state?.crossMenu?.verticalMenu?.nameTextXOffset = 0.0f
                xmbView?.state?.crossMenu?.verticalMenu?.descTextXOffset = 0.0f

                // Update hovering
                items.forEach {
                    it.isHovered = it.id == selectedItemId
                }
                xmbView?.state?.itemMenu?.selectedIndex = 0
                vsh.playSfx(SFXType.Selection)
            }
        }catch (e:ArrayIndexOutOfBoundsException){
            //
        }
    }

    fun doCategorySorting(){
        if(isInRoot){
            val cat = categories.visibleItems.find { it.id == selectedCategoryId }
            if(cat is XMBItemCategory){
                if(cat.sortable){
                    cat.onSwitchSort()
                    xmbView?.state?.crossMenu?.sortHeaderDisplay = 5.25f
                }
            }
        }
    }

    fun launchActiveItem(){
        val item = hoveredItem
        if(item != null){
            if(item.hasContent){
                preventPlayMedia = false
                Logger.d(TAG, "Found content in item ${item.id}, pushing to content stack...")
                if(isInRoot){
                    categories.find { it.id == selectedCategoryId }?.lastSelectedItemId = selectedItemId
                }else{
                    items?.find { it.id == selectedItemId }?.lastSelectedItemId = selectedItemId
                }

                selectedItemId = item.lastSelectedItemId
                selectStack.push(item.id)
                itemOffsetX = 1.0f
                vsh.playSfx(SFXType.Confirm)
            }else{
                preventPlayMedia = true
                item.launch()
            }
        }
    }

    fun backStep(){
        if(selectStack.size > 0){
            preventPlayMedia = false
            selectStack.pull()
            val lastItem = selectedItemId
            selectedItemId = if(selectStack.size == 0) categories.find {it.id == selectedCategoryId}?.lastSelectedItemId ?: "" else selectStack.peek()
            hoveredItem?.lastSelectedItemId =lastItem
            itemOffsetX = -1.0f
            vsh.playSfx(SFXType.Cancel)
        }
    }

    fun isCategoryHidden(id:String) : Boolean = hiddenCategories.find { it == id } != null

    private fun testLoading(){
        val loadHandle = addLoadHandle()
        Handler(Looper.getMainLooper()).postDelayed({ setLoadingFinished(loadHandle) }, 10000L)
    }

    private fun registerInternalCategory(){
        try {
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_HOME, R.string.category_home, R.drawable.category_home, defaultSortIndex = 0))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_SETTINGS, R.string.category_settings, R.drawable.category_setting, defaultSortIndex = 1))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_VIDEO, R.string.category_videos, R.drawable.category_video, true, defaultSortIndex = 2))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_SHORTCUT, R.string.category_shortcut, R.drawable.category_shortcut, true, defaultSortIndex = 2))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_MUSIC, R.string.category_music, R.drawable.category_music, true, defaultSortIndex = 3))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_GAME, R.string.category_games, R.drawable.category_games, true, defaultSortIndex = 4))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_APPS, R.string.category_apps, R.drawable.category_apps, true, defaultSortIndex = 5))
            categories.sortBy { it.sortIndex }
        }catch(e:Exception){

        }
    }

    fun restart() {
        val pm = vsh.packageManager
        val sndi = pm.getLaunchIntentForPackage(vsh.packageName)
        val cmpn = sndi?.component
        if(cmpn!= null){
            val rsti = Intent.makeRestartActivityTask(cmpn)
            vsh.startActivity(rsti)
        }
        exitProcess(0)
    }

    override fun onTerminate() {
        VulkanisirSubmodule.close()
        NativeGL.destroy()
        super.onTerminate()
    }

    fun showAppInfo(app: XMBAppItem) {
        val i = Intent()
        i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        i.data = Uri.fromParts("package", app.resInfo.activityInfo.applicationInfo.packageName, null)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    fun runtimeTriageCheck(id:String) : Boolean {
        if(!runtimeTriageList.contains(id))
        {
            runtimeTriageList.add(id)
            return true
        }
        return false
    }
}
