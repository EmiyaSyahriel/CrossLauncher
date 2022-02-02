package id.psw.vshlauncher

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.net.ConnectivityManager
import android.os.*
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.pluginservices.IconPluginServiceHandle
import id.psw.vshlauncher.submodules.*
import id.psw.vshlauncher.types.*
import id.psw.vshlauncher.views.VshView
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class VSH : Application(), ServiceConnection {

    companion object {
        private lateinit var _input : InputSubmodule
        private lateinit var _adaptIcon : XMBAdaptiveIconRenderer
        private lateinit var _network : NetworkSubmodule
        private lateinit var _appFont : Typeface
        val Input get() = _input
        val IconAdapter get() = _adaptIcon
        val Network get() = _network
        val AppFont get() = _appFont
        const val ACTION_PICK_PLUGIN = "id.psw.vshlauncher.action.PICK_PLUGIN"
        const val CATEGORY_PLUGIN_ICON = "id.psw.vshlauncher.category.PLUGIN_ICON"
        const val CATEGORY_PLUGIN_WAVE_VISUALIZER = "id.psw.vshlauncher.category.PLUGIN_VISUALIZER"
        const val TAG = "VshApp"
        const val ITEM_CATEGORY_HOME = "vsh_home"
        const val ITEM_CATEGORY_APPS = "vsh_apps"
        const val ITEM_CATEGORY_GAME = "vsh_game"
        const val ITEM_CATEGORY_VIDEO = "vsh_video"
        const val ITEM_CATEGORY_MUSIC = "vsh_music"
        const val ITEM_CATEGORY_SETTINGS = "vsh_settings"
    }

    private var appUserUid = 0
    /** Used as user's identifier when using an external storage */
    val UserUid get() = appUserUid

    val selectStack = Stack<String>()
    /** This will change several behaviour :
     * - Application icon and decorative assets will be recycled / unloaded upon hidden from screen
     *
     * Advantage :
     * - Smaller RAM usage
     *
     * Disadvantage :
     * - It will cause a lot of loading
     * - Tend to cause a lot of storage reading access, causing faster medium degradation (usually not a problem)
     */
    var aggressiveUnloading = false
    var vshView : VshView? = null
    var gameFilterList = arrayListOf<String>()
    val categories = arrayListOf<XMBItemCategory>()
    /** Return all item in current selected category or current active item, including the hidden ones */
    val items : ArrayList<XMBItem>? get(){
        var root = categories.visibleItems.find { it.id == selectedCategoryId }?.content
        var i = 0
        while(root != null && i < selectStack.size){
            root = root.find { it.id == selectStack[i]}?.content
            i++
        }
        return root
    }
    val hoveredItem : XMBItem? get() = items?.find { it.id == selectedItemId }

    var selectedCategoryId = ITEM_CATEGORY_APPS
    var selectedItemId = ""

    val itemCursorX get() = categories.visibleItems.indexOfFirst { it.id == selectedCategoryId }
    val itemCursorY get() = (items?.visibleItems?.indexOfFirst { it.id == selectedItemId } ?: -1).coerceAtLeast(0)
    var itemOffsetX = 0.0f
    var itemOffsetY = 0.0f

    val isInRoot : Boolean get() = selectStack.size == 0
    var notificationLastCheckTime = 0L
    val notifications = arrayListOf<XMBNotification>()
    val threadPool: ExecutorService = Executors.newFixedThreadPool(8)
    val loadingHandles = arrayListOf<XMBLoadingHandle>()
    val hiddenCategories = arrayListOf(ITEM_CATEGORY_MUSIC, ITEM_CATEGORY_VIDEO)

    override fun onCreate() {
        notificationLastCheckTime = SystemClock.uptimeMillis()
        _input = InputSubmodule(this)
        _adaptIcon = XMBAdaptiveIconRenderer(this)
        _network = NetworkSubmodule(this)

        registerInternalCategory()
        listInstalledIconPlugins()
        listInstalledWaveRenderPlugins()
        reloadAppList()

        super.onCreate()
    }

    fun moveCursorX(right:Boolean){
        val items = categories.visibleItems
        if(items.size > 0){
            var cIdx = items.indexOfFirst { it.id == selectedCategoryId }
            val oldIdx = cIdx
            items[cIdx].lastSelectedItemId = selectedItemId // Save last category item id
            if(right) cIdx++ else cIdx--
            cIdx = cIdx.coerceIn(0, items.size - 1)
            if(cIdx != oldIdx) itemOffsetX = right.select(1.0f, -1.0f)
            selectedItemId = items[cIdx].lastSelectedItemId // Load new category item id
            selectedCategoryId = items[cIdx].id
        }
    }

    fun moveCursorY(bottom:Boolean){
        try{
            val items = items?.visibleItems
            if(items != null){
                if(items.size > 0){
                    var cIdx = items.indexOfFirst { it.id == selectedItemId }
                    val oldIdx = cIdx
                    if(bottom) cIdx++ else cIdx--
                    cIdx  = cIdx.coerceIn(0, items.size - 1)
                    if(cIdx != oldIdx) itemOffsetY = bottom.select(1.0f, -1.0f);
                    selectedItemId = items[cIdx].id
                }
            }
        }catch (e:ArrayIndexOutOfBoundsException){
            //
        }
    }

    fun isCategoryHidden(id:String) : Boolean = hiddenCategories.find { it == id } != null

    private fun testLoading(){
        val loadHandle = addLoadHandle()
        Handler(Looper.getMainLooper()).postDelayed({ setLoadingFinished(loadHandle) }, 10000L)
    }

    private fun registerInternalCategory(){
        try {
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_HOME, R.string.category_home, R.drawable.category_home))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_SETTINGS, R.string.category_settings, R.drawable.category_setting))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_VIDEO, R.string.category_videos, R.drawable.category_video))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_MUSIC, R.string.category_music, R.drawable.category_music))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_GAME, R.string.category_games, R.drawable.category_games))
            categories.add(XMBItemCategory(this, ITEM_CATEGORY_APPS, R.string.category_apps, R.drawable.category_apps))
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

}
