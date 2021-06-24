package id.psw.vshlauncher

import android.app.Service
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.*
import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Process
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import id.psw.vshlauncher.icontypes.*
import id.psw.vshlauncher.mediaplayer.AudioPlayerSvcConnection
import id.psw.vshlauncher.mediaplayer.XMBVideoPlayer
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.*
import java.io.File
import java.lang.Exception
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

@Suppress("SpellCheckingInspection", "DEPRECATION")
class VSH : AppCompatActivity(), VshDialogView.IDialogBackable {
    companion object{
        var xMarksTheSpot = false
        const val SAVE = "xRegistry"
        const val TAG = "vsh.self"
        var gamePkgPartDictionary = arrayListOf(
            "game",
            "cocos2d",
            "minecraft",
            "dmm", // DMM Games
            "unity", // Unity-made Games
            "games", // Universal (X-plore is included due to LonelyCat{Games})
            "bandai", // Bandai Namco Games
            "typemoon", // Fate VN
            "tencent", // Speed Drifter
            "delightworks" // F/GO
        )
        var storagePermRequest = 5122
        val originLocale = Locale("in", "ID")
        val transparentIcon = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1, Bitmap.Config.ALPHA_8)
        const val DIRLOCK_NONE = 0xab
        const val DIRLOCK_HORIZONTAL = 0xcd
        const val DIRLOCK_VERTICAL = 0xef
        const val PREF_ORIENTATION_KEY = "xmb_orientation"
        const val PREF_MIMIC_USA_CONSOLE = "xmb_PS3_FAT_CECHA00"
        const val PREF_USE_GAMEBOOT = "xmb_USE_GAMEBOOT"
        const val PREF_DYNAMIC_TWINKLE = "xmb_DYNAMIC_P3T"
        const val PREF_IS_FIRST_RUN = "xmb_not_new_user"
        const val PREF_BACKGROUND_COLOR = "xmb_menu_backgroundColor"
        const val PREF_HIDE_CLOCK = "xmb_hideClock"
        const val PREF_SHOW_SEPARATOR = "xmb_showSeparator"
    }

    private var returnFromGameboot = false
    lateinit var prefs : SharedPreferences
    // lateinit var vsh : VshView
    lateinit var vsh : VshServerTestView
    private var storageAllowed = false
    private var sfxPlayer : MediaPlayer? = null
    private var bgmPlayer : MediaPlayer? = null
    private var useGameBoot = false
    private var dynamicThemeTwinkles = true
    private var appListerThread : Thread = Thread()
    var scrOrientation : Int = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    private var isOnMenu = false
    var audioPlayerConnector = AudioPlayerSvcConnection()

    private val mimickedConfirmButton : Int
        get() {
            return xMarksTheSpot.choose(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B)
        }

    private val mimickedCancelButton : Int
        get() {
            return xMarksTheSpot.choose(KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_A)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FontCollections.init(this)

        // vsh = VshView(this)
        vsh = VshServerTestView(this)
        vsh.id = R.id.vsh_view_main
        sysBarTranslucent()
        vsh.fitsSystemWindows = true
        prefs = getSharedPreferences(SAVE, Context.MODE_PRIVATE)
        loadPrefs()
        supportActionBar?.hide()
        initializeMediaPlayer()

        val coldboot = VshColdBoot(this)
        coldboot.fitsSystemWindows = true
        coldboot.onFinishAnimation = Runnable {
            if(prefs.getBoolean(PREF_IS_FIRST_RUN, true)){
                val vshDialog = VshDialogView(this)
                setContentView(vshDialog)
                vshDialog.titleText = getString(R.string.first_run_instruction_title)
                vshDialog.contentText = getString(R.string.first_run_instruction_content)
                val dialogConfirmButton = VshDialogView.Button(
                    getString(android.R.string.ok),
                    Runnable { setContentView(vsh) }
                )
                vshDialog.setButton(arrayListOf(dialogConfirmButton))
                prefs.edit().putBoolean(PREF_IS_FIRST_RUN, false).apply()
            }else{
                setContentView(vsh)
            }
        }

        // setContentView(coldboot)
        setContentView(vsh)
        playColdbootSound()
        setOperatorName()

        checkFileReadWritePermission()
        initServerData()
        appListerThread = Thread( Runnable {
            loadApps()
        })
        appListerThread.start()
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        recalculateChooseRect()
        VshServer.recalculateClockRect()
        populateSettingSections()

        getRenderableScreen()
    }

    private fun playColdbootSound(){
        try{
            val dataFolder = getExternalFilesDir(null)?: return
            val audioFile = dataFolder.listFiles()?.find { it.name.toLowerCase(Locale.ROOT) == "coldboot.mp3" } ?: return
            if(audioFile.exists()){
                sfxPlayer?.reset()
                sfxPlayer?.setDataSource(audioFile.absolutePath)
                sfxPlayer?.prepare()
                sfxPlayer?.start()
            }
        }finally {
            // Bad practice, mari lestarikan hohooho
        }
    }

    private fun getRenderableScreen(){
        val offsetRect = Rect()
        window.decorView.getLocalVisibleRect(offsetRect)
        VshView.padding.top = offsetRect.top.toFloat()
        VshView.padding.left = offsetRect.left.toFloat()
        VshView.padding.right = offsetRect.right.toFloat()
        VshView.padding.bottom = offsetRect.bottom.toFloat()
        Log.d(TAG, "Found Screen Data = $offsetRect")
    }

    private fun playGamebootSound(){
        try{
            val dataFolder = getExternalFilesDir(null)?: return
            val audioFile = dataFolder.listFiles()?.find { it.name.toLowerCase(Locale.ROOT) == "gameboot.mp3" } ?: return
            if(audioFile.exists()){
                sfxPlayer?.reset()
                sfxPlayer?.setDataSource(audioFile.absolutePath)
                sfxPlayer?.prepare()
                sfxPlayer?.start()
            }
        }finally {
            // Bad practice, mari lestarikan hohooho
        }
    }

    private fun initializeMediaPlayer(){
        sfxPlayer = MediaPlayer()
        sfxPlayer?.setOnPreparedListener { it.start() }
        sfxPlayer?.setOnCompletionListener { VshServer.StatusBar.clockExpandInfo = "" }
    }

    private fun loadPrefs(){
        xMarksTheSpot = prefs.getBoolean(PREF_MIMIC_USA_CONSOLE, false)
        scrOrientation = prefs.getInt(PREF_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        useGameBoot = prefs.getBoolean(PREF_USE_GAMEBOOT, true)
        dynamicThemeTwinkles = prefs.getBoolean(PREF_DYNAMIC_TWINKLE, true)
        VshView.menuBackgroundColor = prefs.getInt(PREF_BACKGROUND_COLOR, Color.argb(0,0,0,0))
        VshView.hideClock = prefs.getBoolean(PREF_HIDE_CLOCK, false)
        VshView.descriptionSeparator = prefs.getBoolean(PREF_SHOW_SEPARATOR, false)
    }

    private fun checkFileReadWritePermission(){
        val resultRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val resultWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(resultRead == PackageManager.PERMISSION_GRANTED && resultWrite == PackageManager.PERMISSION_GRANTED){
            storageAllowed = true
        }else{
            requestPermission()
        }
    }

    override fun onPause() {
        returnFromGameboot = true
        super.onPause()
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), storagePermRequest)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            storagePermRequest -> {
                if(grantResults.isNotEmpty() && grantResults.all{ it == PackageManager.PERMISSION_GRANTED}){
                    storageAllowed = true
                }else{
                    Toast.makeText(this, getString(R.string.storage_permission_not_granted), Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        VshServer.recalculateClockRect()
        getRenderableScreen()
    }

    ///region Orientation Settings
    private fun setOrientation(orientation:Int){
        scrOrientation = orientation
        requestedOrientation = orientation
        prefs.edit().putInt(PREF_ORIENTATION_KEY, scrOrientation).apply()
    }

    private fun switchOrientation(){
        scrOrientation = when(scrOrientation){
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_USER
            ActivityInfo.SCREEN_ORIENTATION_USER -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_USER
        }
        requestedOrientation = scrOrientation
        prefs.edit().putInt(PREF_ORIENTATION_KEY, scrOrientation).apply()
    }

    private fun setCurrentOrientation(orientation:Int){
        try{
            scrOrientation = orientation
            requestedOrientation = scrOrientation
            prefs.edit().putInt(PREF_ORIENTATION_KEY, scrOrientation).apply()
        }catch(e:Exception){
            toast(e.message)
        }
    }

    private fun toast(string:String?, long:Boolean = false){
        Toast
            .makeText(this, string, long.choose(Toast.LENGTH_LONG,Toast.LENGTH_SHORT))
            .show()
    }

    private fun getOrientationName() : String{
        return getString(orientationName[requestedOrientation] ?: R.string.orient_unknown)
    }

    private fun getOrientationName(id:Int):String{
        return getString(orientationName[id] ?: R.string.orient_unknown)
    }

    ///endregion

    private fun setGameBoot(){
        useGameBoot = !useGameBoot
        prefs.edit().putBoolean(PREF_USE_GAMEBOOT, useGameBoot).apply()
    }

    private fun setGameBoot(active:Boolean){
        useGameBoot = active
        prefs.edit().putBoolean(PREF_USE_GAMEBOOT, useGameBoot).apply()
    }

    private fun setTwinkles(){
        dynamicThemeTwinkles = !dynamicThemeTwinkles
        prefs.edit().putBoolean(PREF_DYNAMIC_TWINKLE, useGameBoot).apply()
    }

    private fun setTwinkles(active:Boolean){
        dynamicThemeTwinkles = active
        prefs.edit().putBoolean(PREF_DYNAMIC_TWINKLE, useGameBoot).apply()
    }

    private fun Boolean.toLocalizedString() : String{
        return if(this) getString(R.string.common_yes) else getString(R.string.common_no)
    }



    private fun populateSettingSections(){
        // TODO
    }

    private fun restartApp(){
        val starter = Intent(this, VSH::class.java)
        val intentId = Process.myPid()
        val pendingIntent = PendingIntent.getActivity(this, intentId, starter, PendingIntent.FLAG_CANCEL_CURRENT )
        val mgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent)
        exitProcess(0)
    }

    private fun switchToRefreshRequestWindow(){
        val alert = VshDialogView(this)
        alert.buttons.clear()
        alert.buttons.add(VshDialogView.Button("OK", Runnable {
            val thread = runOnOtherThread { loadApps() }
            setContentView(vsh)
        }))
        alert.buttons.add(
            VshDialogView.Button("Cancel",
                Runnable {
                    setContentView(vsh)
                }
            ))
        alert.titleText = "Rebuilding App Database"
        alert.contentText = "Application database will be rebuilt, this launcher will not\nbe usable until it finished."
        alert.iconBitmap = resources.getDrawable(R.drawable.icon_refresh).toBitmap(32,32)
        setContentView(alert)
    }

    private val orientationName = mapOf(
        Pair(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, R.string.orient_portrait),
        Pair(ActivityInfo.SCREEN_ORIENTATION_USER, R.string.orient_user),
        Pair(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, R.string.orient_landscape)
    )

    private fun setOperatorName() {
        val telephonyManager = getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        VshServer.StatusBar.operatorName = telephonyManager.simOperatorName
        VshServer.StatusBar.use24Format = android.text.format.DateFormat.is24HourFormat(this)
    }

    private fun sysBarTranslucent(){
        if(Build.VERSION.SDK_INT >= 19){
            window.apply {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                if(Build.VERSION.SDK_INT >= 21){
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    statusBarColor = Color.TRANSPARENT
                }
                if(Build.VERSION.SDK_INT >= 28){
                    attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
    }

    override fun onBackPressed() {
        VshServer.sendBackSignal()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(!isOnMenu) return false
        var retval = false

        when(keyCode){
            KeyEvent.KEYCODE_DPAD_UP -> {
                VshServer.Input.onKeyDown(VshServer.InputKeys.DPadU)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                VshServer.Input.onKeyDown(VshServer.InputKeys.DPadD)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                VshServer.Input.onKeyDown(VshServer.InputKeys.DPadL)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT ->{
                VshServer.Input.onKeyDown(VshServer.InputKeys.DPadR)
                retval = true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER ->{
                VshServer.Input.onKeyDown(VshServer.InputKeys.Select)
                retval = true
            }
            KeyEvent.KEYCODE_DEL ->{
                VshServer.Input.onKeyDown(VshServer.InputKeys.Back)
                retval = true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_BUTTON_Y -> {
                VshServer.Input.onKeyDown(VshServer.InputKeys.Menu)
                retval = true
            }
        }

        return retval || super.onKeyDown(keyCode, event)
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if(!isOnMenu) return false
        var retval = false

        when(keyCode){
            KeyEvent.KEYCODE_DPAD_UP -> {
                VshServer.Input.onKeyUp(VshServer.InputKeys.DPadU)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                VshServer.Input.onKeyUp(VshServer.InputKeys.DPadD)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                VshServer.Input.onKeyUp(VshServer.InputKeys.DPadL)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT ->{
                VshServer.Input.onKeyUp(VshServer.InputKeys.DPadR)
                retval = true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER ->{
                VshServer.Input.onKeyUp(VshServer.InputKeys.Select)
                retval = true
            }
            KeyEvent.KEYCODE_DEL ->{
                VshServer.Input.onKeyUp(VshServer.InputKeys.Back)
                retval = true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_BUTTON_Y -> {
                VshServer.Input.onKeyUp(VshServer.InputKeys.Menu)
                retval = true
            }
        }

        return retval || super.onKeyUp(keyCode, event)
    }

    private fun initServerData(){
        if(VshServer.findCategory(VshCategory.apps) == null){
            val apps= XMBLambdaIcon(VshCategory.apps).apply {
                nameImpl = { VshCategory.apps }
            }
            VshServer.root.addContent(apps)
        }

        if(VshServer.findCategory(VshCategory.games) == null){
            val games = XMBLambdaIcon(VshCategory.games).apply {
                nameImpl = { VshCategory.games }
            }
            VshServer.root.addContent(games)
        }
    }

    private fun loadApps(){
        val apps = VshServer.findCategory(VshCategory.apps) ?: return
        val games = VshServer.findCategory(VshCategory.games) ?: return
        apps.content.clear()
        games.content.clear()
        VshServer.StatusBar.isLoading = true

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resInfo = packageManager.queryIntentActivities(intent, 0)

        resInfo.forEachIndexed { index, it ->
            if(!appListerThread.isInterrupted){
                val isGame = packageIsGame(it.activityInfo)
                //println("VTX_Activity [I] | New App : ${appData.name} (${appData.pkg}) - isGame : $isGame")
                //val size = File(it.activityInfo.applicationInfo.sourceDir).length().toSize()
                val pkg = it.activityInfo.packageName
                val xmbData = XMBAppIcon(pkg, it, this)

                // Filter itself
                if(it.activityInfo.packageName != packageName){
                    (if(isGame){games}else{apps}).addContent(xmbData)
                }
            }
        }

        apps.content.sortBy { it.name }
        games.content.sortBy { it.name }

        VshServer.StatusBar.isLoading = false
        apps.selectedIndex = 10
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            // Reload when uninstall is requested
            UNINSTALL_REQ_CODE -> if(resultCode == RESULT_OK) runOnOtherThread { loadApps() }
        }
    }

    fun startApp(packageName: String){
        VshServer.ContextMenu.visible = false
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if(null != intent){
            if(useGameBoot){
                val gameBoot = VshGameBoot(this)
                gameBoot.showTwinkles = dynamicThemeTwinkles
                playGamebootSound()
                gameBoot.onFinishAnimation =  Runnable {
                    startActivity(intent)
                    overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
                }
                setContentView(gameBoot)
            }else{
                startActivity(intent)
                overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
            }
        }else{
            val v = VshDialogView(this)
            v.setButton(arrayListOf(VshDialogView.Button(getString(android.R.string.ok)) {
                setContentView(vsh)
            }
            ))
            v.contentText = "Cannot start this application.\n(NameNotFoundException : Not Installed)"
            v.titleText = "Launch Error"
            setContentView(v)
        }
    }

    override fun onResume() {
        super.onResume()
        if(returnFromGameboot){
            FontCollections.init(this)
            setContentView(vsh)
        }
    }

    fun reloadContent(){
        appListerThread.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
    }

    private fun packageIsGame(activityInfo: ActivityInfo): Boolean {
        var retval: Boolean

        var api29 = false
        var apiPre29 = false
        val byPackageName: Boolean
        try {
            val info: ApplicationInfo = packageManager.getApplicationInfo(activityInfo.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                api29 = info.category == ApplicationInfo.CATEGORY_GAME
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                apiPre29 = (info.flags and ApplicationInfo.FLAG_IS_GAME) == ApplicationInfo.FLAG_IS_GAME
            }

            byPackageName = packageIsGameByDB(activityInfo)

            retval = api29 || apiPre29 || byPackageName

        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "No package info for ${activityInfo.packageName}", e)
            // Or throw an exception if you want
            retval = false
        }

        return retval
    }

    override fun onContentChanged() {
        val data = findViewById<View>(R.id.vsh_view_main)
        isOnMenu = null != data
        Log.d(TAG, "Menu is ${isOnMenu.choose("Shown","Hidden")}")
        super.onContentChanged()
    }

    // TODO: Also add an user editable dictionary
    private fun packageIsGameByDB(appinfo : ActivityInfo) : Boolean{
        val lwr = appinfo.name.toLowerCase(originLocale)
        var retval = false

        gamePkgPartDictionary.forEach{
            retval = retval || lwr.contains(it)
        }

        return retval
    }

    var touchStartPoint = PointF(0f,0f)
    var touchDeltaStartPoint = PointF(0f,0f)
    var touchCurrentPoint = PointF(0f,0f)
    var launchArea = RectF(0f,0f,0f,0f)
    var directionLock = DIRLOCK_NONE
    var touchSlop = 10
    var isDrag = false

    fun recalculateChooseRect(){
        VshServer.Input.recalculateLaunchPos()
    }

    private var touchCount = 0
    private var lastTouchTime = 0L
    private var inputTimer = Timer()
    private fun onTouchCountChange(now:Int, last:Int, timeDelta:Long){
        // Switch vsh option when two tap is detected and less than 0.2s difference
        if(last < now && now == 2 && timeDelta < 100L){
            // TODO:
        }

        // Reset touch count after a second
        inputTimer.schedule(1000L){ touchCount = 0 }
    }

    private var points : HashMap<Int, PointF> = HashMap()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var retval = false
        val idx = event.actionIndex
        val id= event.getPointerId(0)
        val x = event.getX(idx)
        val y = event.getY(idx)
        when(event.actionMasked){
            MotionEvent.ACTION_UP, MotionEvent.ACTION_HOVER_EXIT, MotionEvent.ACTION_POINTER_UP ->{
                points.remove(id)
                VshServer.Input.onTouchUp(id,PointF(x,y))
                retval = true
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_POINTER_DOWN -> {
                points.put(id, PointF(x,y))
                VshServer.Input.onTouchDown(id,PointF(x,y))
                retval = true
            }
            MotionEvent.ACTION_MOVE ->{
                points[id]?.x = x
                points[id]?.y = y
                VshServer.Input.onTouchMove(id,PointF(x,y))
                retval = true
            }
        }

        return retval || super.onTouchEvent(event)
    }

    override fun onDialogBack() {
        setContentView(vsh)
        touchCount = 0
        lastTouchTime = System.currentTimeMillis()
    }

}
