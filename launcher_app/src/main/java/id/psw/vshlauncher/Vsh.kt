package id.psw.vshlauncher

import android.app.ActivityManager
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jakewharton.threetenabp.AndroidThreeTen
import id.psw.vshlauncher.livewallpaper.NativeGL
import id.psw.vshlauncher.submodules.*
import id.psw.vshlauncher.types.*
import id.psw.vshlauncher.types.Stack
import id.psw.vshlauncher.types.items.XmbAppItem
import id.psw.vshlauncher.types.items.XmbItemCategory
import id.psw.vshlauncher.types.media.LinearMediaList
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

class Vsh : Application() {

    companion object {
        private lateinit var _appFont : Typeface
        val AppFont get() = _appFont
        const val TAG = "VshApp"
        const val ITEM_CATEGORY_HOME = "vsh_home"
        const val ITEM_CATEGORY_APPS = "vsh_apps"
        const val ITEM_CATEGORY_GAME = "vsh_game"
        const val ITEM_CATEGORY_VIDEO = "vsh_video"
        const val ITEM_CATEGORY_SHORTCUT = "vsh_shortcut"
        const val ITEM_CATEGORY_PHOTO = "vsh_photos"
        const val ITEM_CATEGORY_MUSIC = "vsh_music"
        const val ITEM_CATEGORY_SETTINGS = "vsh_settings"
        const val COPY_DATA_SIZE_BUFFER = 10240
        const val ACT_REQ_INSTALL_PACKAGE = 4496
        const val ACT_REQ_MEDIA_LISTING = 0x9121

        val dbgMemInfo = Debug.MemoryInfo()
        val actMemInfo = ActivityManager.MemoryInfo()
    }

    var showDebuggerCount = 0

    private var appUserUid = 0
    /** Used as user's identifier when using an external storage */
    val UserUid get() = appUserUid

    val selectStack = Stack<String>()
    var aggressiveUnloading = true
    val runtimeTriageList = ArrayList<String>()
    var xmbView : XmbView? = null

    val haveXmbView get() = xmbView != null
    val safeXmbView get()= xmbView!!

    var playAnimatedIcon = true
    var mediaListingStarted = false
    lateinit var mainHandle : Handler

    val linearMediaList = LinearMediaList()

    val categories = arrayListOf<XmbItemCategory>()
    /** Return all item in current selected category or current active item, including the hidden ones */
    val items : ArrayList<XmbItem>? get(){
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
        return arrayListOf()
    }

    val activeParent : XmbItem? get(){
        var root = categories.visibleItems.find { it.id == selectedCategoryId }
        var i = 0
        while(root != null && i < selectStack.size){
            root = root.content?.find { it.id == selectStack[i]}
            i++
        }
        return root
    }

    val hoveredItem : XmbItem? get() = items?.find { it.id == selectedItemId }
    private var _waveShouldRefresh = false
    var waveShouldReReadPreferences : Boolean get() {
        val r = _waveShouldRefresh
        _waveShouldRefresh = false
        return r
    }

    set(v) { _waveShouldRefresh = v }

    var selectedCategoryId = ITEM_CATEGORY_APPS
    var selectedItemId = ""

    val itemCursorX get() = categories.visibleItems.indexOfFirst { it.id == selectedCategoryId }
    val itemCursorY get() = (items?.visibleItems?.filterBySearch(this)?.indexOfFirst { it.id == selectedItemId } ?: -1).coerceAtLeast(0)
    var itemOffsetX = 0.0f
    var itemOffsetY = 0.0f
    var itemBackdropAlphaTime = 0.0f
    var _prioritizeTvIntent = false

    val isInRoot : Boolean get() = selectStack.size == 0
    var notificationLastCheckTime = 0L
    val notifications = arrayListOf<XmbNotification>()
    val threadPool: ExecutorService = Executors.newFixedThreadPool(8)
    val loadingHandles = arrayListOf<XmbLoadingHandle>()
    val hiddenCategories = arrayListOf<String>()

    val M = SubmoduleManager(this)
    var isTv = false
    var shouldShowExitOption = false

    var isNowRendering = false

    var useInternalWave = true
    var lifeScope : LifecycleCoroutineScope = ProcessLifecycleOwner.get().lifecycleScope

    private fun reloadPreference() {
        setActiveLocale(readSerializedLocale(M.pref.get(PrefEntry.SYSTEM_LANGUAGE, "")))
        CifLoader.videoIconMode = VideoIconMode.fromInt(M.pref.get(PrefEntry.VIDEO_ICON_PLAY_MODE, 0))
        val o = M.pref.get(PrefEntry.SYSTEM_VISIBLE_APP_DESC, XmbAppItem.DescriptionDisplay.PackageName.ordinal)
        XmbAppItem.descriptionDisplay = enumFromInt(o)
        XmbAdaptiveIconRenderer.Companion.AdaptiveRenderSetting.iconPriority =
            M.pref.get(PrefEntry.ICON_RENDERER_PRIORITY, 0b01111000)
        val tvAsDefault = isTv.select(1, 0)
        _prioritizeTvIntent = M.pref.get(PrefEntry.LAUNCHER_TV_INTENT_FIRST, tvAsDefault) != 0
    }

    override fun onCreate() {
        Logger.init(this)
        AndroidThreeTen.init(this)
        mainHandle = Handler(mainLooper)
        isTv = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
        }else{
            packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
        }

        M.onCreate()
        reloadPreference()
        FontCollections.init(this)
        // Fresco.initialize(this)
        notificationLastCheckTime = SystemClock.uptimeMillis()
        registerInternalCategory()
        reloadAppList()
        reloadShortcutList()
        fillSettingsCategory()
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
            M.audio.preventPlayMedia = false
            var cIdx = items.indexOfFirst { it.id == selectedCategoryId }
            cIdx = cIdx.coerceIn(0, items.size - 1)
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
            xmbView?.widgets?.sideMenu?.selectedIndex = 0
            M.audio.playSfx(SfxType.Selection)

            xmbView?.screens?.mainMenu?.verticalMenu?.nameTextXOffset = 0.0f
            xmbView?.screens?.mainMenu?.verticalMenu?.descTextXOffset = 0.0f
        }
    }

    fun queryTexture(customId:String) : ArrayList<File> =FileQuery(VshBaseDirs.VSH_RESOURCES_DIR).withNames("$customId.png").execute(this)

    fun loadTexture(@DrawableRes d : Int, w:Int, h:Int, whiteFallback:Boolean = false ) : Bitmap {
        return ResourcesCompat.getDrawable(resources, d, null)?.toBitmap(w, h) ?: whiteFallback.select(XmbItem.WHITE_BITMAP, XmbItem.TRANSPARENT_BITMAP)
    }
    fun loadTexture(@DrawableRes d : Int, whiteFallback:Boolean = false ) : Bitmap {
        val dwb =ResourcesCompat.getDrawable(resources, d, null)
        return dwb?.toBitmap(dwb.intrinsicWidth, dwb.intrinsicHeight?: 1) ?: whiteFallback.select(XmbItem.WHITE_BITMAP, XmbItem.TRANSPARENT_BITMAP)
    }

    val allAppEntries = arrayListOf<XmbAppItem>()

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
                M.audio.preventPlayMedia = false
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

                xmbView?.screens?.mainMenu?.verticalMenu?.nameTextXOffset = 0.0f
                xmbView?.screens?.mainMenu?.verticalMenu?.descTextXOffset = 0.0f

                // Update hovering
                items.forEach {
                    it.isHovered = it.id == selectedItemId
                }
                xmbView?.widgets?.sideMenu?.selectedIndex = 0
                M.audio.playSfx(SfxType.Selection)
            }
        }catch (e:ArrayIndexOutOfBoundsException){
            //
        }
    }

    fun doCategorySorting(){
        if(isInRoot){
            val cat = categories.visibleItems.find { it.id == selectedCategoryId }
            if(cat is XmbItemCategory){
                if(cat.sortable){
                    cat.onSwitchSort()
                    xmbView?.screens?.mainMenu?.sortHeaderDisplay = 5.25f
                }
            }
        }
    }

    fun launchActiveItem(){
        val item = hoveredItem
        if(item != null){
            if(item.hasContent){
                M.audio.preventPlayMedia = false
                Logger.d(TAG, "Found content in item ${item.id}, pushing to content stack...")
                if(isInRoot){
                    categories.find { it.id == selectedCategoryId }?.lastSelectedItemId = selectedItemId
                }else{
                    items?.find { it.id == selectedItemId }?.lastSelectedItemId = selectedItemId
                }

                selectedItemId = item.lastSelectedItemId
                selectStack.push(item.id)
                itemOffsetX = 1.0f
                M.audio.playSfx(SfxType.Confirm)
            }else{
                M.audio.preventPlayMedia = true
                item.launch()
            }
        }
    }

    fun backStep(){
        if(selectStack.size > 0){
            M.audio.preventPlayMedia = false
            selectStack.pull()
            val lastItem = selectedItemId
            selectedItemId = if(selectStack.size == 0) categories.find {it.id == selectedCategoryId}?.lastSelectedItemId ?: "" else selectStack.peek()
            hoveredItem?.lastSelectedItemId =lastItem
            itemOffsetX = -1.0f
            M.audio.playSfx(SfxType.Cancel)
        }
    }

    fun isCategoryHidden(id:String) : Boolean = hiddenCategories.find { it == id } != null

    private fun registerInternalCategory(){
        try {
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_HOME, R.string.category_home, R.drawable.category_home, defaultSortIndex = 0))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_SETTINGS, R.string.category_settings, R.drawable.category_setting, defaultSortIndex = 1))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_VIDEO, R.string.category_videos, R.drawable.category_video, true, defaultSortIndex = 2))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_SHORTCUT, R.string.category_shortcut, R.drawable.category_shortcut, true, defaultSortIndex = 2))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_PHOTO, R.string.category_photo, R.drawable.category_photo, true, defaultSortIndex = 3))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_MUSIC, R.string.category_music, R.drawable.category_music, true, defaultSortIndex = 4))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_GAME, R.string.category_games, R.drawable.category_games, true, defaultSortIndex = 5))
            categories.add(XmbItemCategory(this, ITEM_CATEGORY_APPS, R.string.category_apps, R.drawable.category_apps, true, defaultSortIndex = 6))
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
        M.onDestroy()
        VulkanisirSubmodule.close()
        NativeGL.destroy()
        super.onTerminate()
    }

    fun showAppInfo(app: XmbAppItem) {
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
    fun openFileByDefaultApp(apk: File) {
        if(haveXmbView){
            val xmb =safeXmbView.context.xmb
            xmb.runOnUiThread {
                val authority = BuildConfig.APPLICATION_ID + ".fileprovider"
                val u = FileProvider.getUriForFile(xmb, authority, apk)
                val i = Intent(Intent.ACTION_VIEW)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                i.setDataAndType(u, contentResolver.getType(u))
                i.putExtra(Intent.EXTRA_STREAM, u)
                i.data = u
                xmb.startActivity(i)
            }
        }

    }
}
