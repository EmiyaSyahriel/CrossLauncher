package id.psw.vshlauncher

import android.app.ActivityManager
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.livewallpaper.NativeGL
// import com.facebook.drawee.backends.pipeline.Fresco
import id.psw.vshlauncher.pluginservices.IconPluginServiceHandle
import id.psw.vshlauncher.submodules.*
import id.psw.vshlauncher.types.*
import id.psw.vshlauncher.types.Stack
import id.psw.vshlauncher.types.items.XMBAppItem
import id.psw.vshlauncher.types.items.XMBItemCategory
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.filterBySearch
import junit.runner.Version.id
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class VSH : Application(), ServiceConnection {

    companion object {
        private lateinit var _appFont : Typeface
        val AppFont get() = _appFont
        const val ACTION_PICK_PLUGIN = "id.psw.vshlauncher.action.PICK_PLUGIN"
        const val CATEGORY_PLUGIN_ICON = "id.psw.vshlauncher.category.PLUGIN_ICON"
        const val CATEGORY_PLUGIN_WAVE_VISUALIZER = "id.psw.vshlauncher.category.PLUGIN_VISUALIZER"
        const val TAG = "VshApp"
        const val ITEM_CATEGORY_HOME = "vsh_home"
        const val ITEM_CATEGORY_APPS = "vsh_apps"
        const val ITEM_CATEGORY_GAME = "vsh_game"
        const val ITEM_CATEGORY_VIDEO = "vsh_video"
        const val ITEM_CATEGORY_SHORTCUT = "vsh_shortcut"
        const val ITEM_CATEGORY_MUSIC = "vsh_music"
        const val ITEM_CATEGORY_SETTINGS = "vsh_settings"
        const val COPY_DATA_SIZE_BUFFER = 10240

        val dbgMemInfo = Debug.MemoryInfo()
        val actMemInfo = ActivityManager.MemoryInfo()
    }

    lateinit var _gamepad : GamepadSubmodule
    var showDebuggerCount = 0
    var _gamepadUi : GamepadUISubmodule = GamepadUISubmodule()
    lateinit var iconAdapter : XMBAdaptiveIconRenderer
    lateinit var network : NetworkSubmodule

    private var appUserUid = 0
    /** Used as user's identifier when using an external storage */
    val UserUid get() = appUserUid

    val selectStack = Stack<String>()
    var aggressiveUnloading = true
    var xmbView : XmbView? = null
    var playAnimatedIcon = true
    var gameFilterList = arrayListOf<String>(
        "bandai",
        "cygames",
        "sudoku",
        "umamusume",
        "unity"
    )
    val categories = arrayListOf<XMBItemCategory>()
    /** Return all item in current selected category or current active item, including the hidden ones */
    val items : ArrayList<XMBItem>? get(){
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

    val bgmPlayer = MediaPlayer()
    val systemBgmPlayer = MediaPlayer()
    lateinit var bgmPlayerActiveSrc : File
    var bgmPlayerDoNotAutoPlay = false
    val sfxPlayer = SoundPool(10, AudioManager.STREAM_SYSTEM, 0)
    val sfxIds = mutableMapOf<SFXType, Int>()
    var shouldShowExitOption = false

    var useInternalWave = true

    fun reloadPreference() {
        pref = getSharedPreferences("xRegistry.sys", Context.MODE_PRIVATE)
        setActiveLocale(readSerializedLocale(pref.getString(PrefEntry.SYSTEM_LANGUAGE, "") ?: ""))
        XMBAppItem.disableAnimatedIcon = pref.getInt(PrefEntry.DISPLAY_VIDEO_ICON, 1) == 0
    }

    override fun onCreate() {
        reloadPreference()
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
        listInstalledIconPlugins()
        listInstalledWaveRenderPlugins()
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
        Log.d(TAG, sb.toString())
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

    fun loadTexture(@DrawableRes d : Int, w:Int, h:Int, whiteFallback:Boolean = false ) : Bitmap {
        return ResourcesCompat.getDrawable(resources, d, null)?.toBitmap(w, h) ?: whiteFallback.select(XMBItem.WHITE_BITMAP, XMBItem.TRANSPARENT_BITMAP)
    }
    fun loadTexture(@DrawableRes d : Int, whiteFallback:Boolean = false ) : Bitmap {
        val dwb =ResourcesCompat.getDrawable(resources, d, null)
        return dwb?.toBitmap(dwb.intrinsicWidth ?: 1, dwb.intrinsicHeight?: 1) ?: whiteFallback.select(XMBItem.WHITE_BITMAP, XMBItem.TRANSPARENT_BITMAP)
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
                Log.d(TAG, "Found content in item ${item.id}, pushing to content stack...")
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

    private fun createPluginIntent(category: String) : Intent {
        return Intent(ACTION_PICK_PLUGIN)
            .setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION)
            .addCategory(category)
    }

    private fun listInstalledWaveRenderPlugins(){
        val baseIntent = createPluginIntent(CATEGORY_PLUGIN_WAVE_VISUALIZER)
        val resolves = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER)
        for(i in resolves){
            Log.d(TAG, "Vis Plugin : ${i.serviceInfo.packageName} / ${i.serviceInfo.name} - ${i.serviceInfo.loadLabel(packageManager)}")
            val dIntent = Intent(ACTION_PICK_PLUGIN).addCategory(CATEGORY_PLUGIN_WAVE_VISUALIZER).setComponent(
                ComponentName(i.serviceInfo.packageName, i.serviceInfo.name)
            )
            if(!bindService(dIntent, this, Context.BIND_AUTO_CREATE)){
                Log.e(TAG, "Failed to icon bind service")
            }else{
                Log.d(TAG, "Bind success")
            }
        }
    }

    val iconPlugins : ArrayList<IconPluginServiceHandle> = arrayListOf()

    private fun listInstalledIconPlugins(){
        val baseIntent = createPluginIntent(CATEGORY_PLUGIN_ICON)
        val resolves = packageManager.queryIntentServices(baseIntent, PackageManager.GET_RESOLVED_FILTER)
        for(i in resolves){
            Log.d(TAG, "Icon Plugin : ${i.serviceInfo.packageName} / ${i.serviceInfo.name}  - ${i.serviceInfo.loadLabel(packageManager)}")
            val cName = ComponentName(i.serviceInfo.packageName, i.serviceInfo.name)

            iconPlugins.add(IconPluginServiceHandle(className = cName))

            val dIntent = Intent(ACTION_PICK_PLUGIN)
                .addCategory(CATEGORY_PLUGIN_ICON)
                .setComponent(cName)


            if(!bindService(dIntent, this, Context.BIND_AUTO_CREATE)){
                Log.e(TAG, "Failed to icon bind service")
            }else{
                Log.d(TAG, "Bind success")
            }
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val asIcon = iconPlugins.find { it -> it.className == name }
        if(asIcon != null){
            val binder = IXMBIconListProvider.Stub.asInterface(service)
            asIcon.apply {
                this.name = binder.name
                this.description = binder.description
                this.version = binder.versionString
                this.disabled = false
                this.provider = binder
            }
            Log.d(TAG, "Icon Plugin Connected : ${name?.className} :: ${binder.name} (${binder.versionString}) - ${binder.description}")
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        val iconIdx = iconPlugins.indexOfFirst { it.className == name }
        if(iconIdx > -1){
            iconPlugins.removeAt(iconIdx)
            Log.d(TAG, "Plugin Disconnected : ${name?.className}")
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
}
