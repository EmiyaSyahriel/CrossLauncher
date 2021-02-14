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
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.contains
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.icontypes.*
import id.psw.vshlauncher.mediaplayer.AudioPlayerSvcConnection
import id.psw.vshlauncher.mediaplayer.XMBVideoPlayer
import id.psw.vshlauncher.typography.FontCollections
import id.psw.vshlauncher.views.VshColdBoot
import id.psw.vshlauncher.views.VshDialogView
import id.psw.vshlauncher.views.VshGameBoot
import id.psw.vshlauncher.views.VshView
import java.io.File
import java.lang.Exception
import kotlin.concurrent.schedule

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
    lateinit var vsh : VshView
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

        vsh = VshView(this)
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

        setContentView(coldboot)
        playColdbootSound()

        setOperatorName()

        checkFileReadWritePermission()
        appListerThread = Thread( Runnable {
            loadApps()
            loadAudio()
            loadVideo()
        })
        appListerThread.start()
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        recalculateChooseRect()
        vsh.recalculateClockRect()
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
        sfxPlayer?.setOnCompletionListener { vsh.clockExpandInfo = "" }
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
        vsh.recalculateClockRect()
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
        val settings = vsh.findById("SETT") ?: return

        val y = true.toLocalizedString()
        val n = false.toLocalizedString()
        val blankIcon = Icon.fromBitmap(XMBIcon.TransparentBitmap)

        // Orientation
        val systemSetting = VshSettingCategory(this, vsh, "xmb_setting_system", "System Setting", "", blankIcon)
        settings.addContent(systemSetting)

        systemSetting.add(
            VshOptionedSettingIcon(
                0xd1802, this, getString(R.string.item_orientation), VshSettingIcon.DEVICE_ORIENTATION,
                { switchOrientation() }, { getOrientationName() },
                { XMBIcon.MenuEntryBuilder(systemSetting, "Sys")
                        .add(getOrientationName(ActivityInfo.SCREEN_ORIENTATION_USER)) { setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER) }
                        .add(getOrientationName(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)) { setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) }
                        .add(getOrientationName(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)) { setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) }
                        .build() }
            )
        )

        setOrientation(prefs.getInt(PREF_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE))

        // TODO : Add cust. icon
        val displaySetting = VshSettingCategory(this, vsh, "xmb_icon_display", "Display Setting", "", blankIcon)
        settings.addContent(displaySetting)

        displaySetting.add(VshOptionedSettingIcon(
            0xd1803, this,
            getString(R.string.setting_show_gameboot),
            VshSettingIcon.ICON_ANDROID,
            { setGameBoot() },
            { useGameBoot.toLocalizedString() },
            {
                VshY.VshOptionsBuilder()
                    .add(y){ setGameBoot(true) }
                    .add(n){ setGameBoot(false) }
                    .build()
            }
        ))

        displaySetting.

        // Dynamic Twinkle Icon
        displaySetting.add(VshOptionedSettingIcon(
            0xd1804, this,
            getString(R.string.setting_mimic_dynamic_theme),VshSettingIcon.ICON_STAR,
            { setTwinkles() },
            { dynamicThemeTwinkles.toLocalizedString() },
            {
                VshY.VshOptionsBuilder()
                    .add(y){ setTwinkles(true) }
                    .add(n){ setTwinkles(false) }
                    .build()
            }
        ))

        displaySetting.add(
            VshOptionedSettingIcon(
                0xd1805, this,
                "Set Menu Background Color", VshSettingIcon.ICON_ANDROID,
                { showBackgroundColorDialog() },
                { "Menu background (hidden when menu is hidden)" },
                { preMadeColors.build() }
            )
        )

        displaySetting.add(
            VshOptionedSettingIcon(
                0xd1806, this,
                "Hide Clock Bar", VshSettingIcon.ICON_ANDROID,
                { setHiddenClock() },
                { VshView.hideClock.toLocalizedString() },
                { VshY.VshOptionsBuilder()
                    .add(y){ setHiddenClock(true) }
                    .add(n){ setHiddenClock(false) }
                    .build() }
            )
        )


        displaySetting.add(
            VshOptionedSettingIcon(
                0xd1807, this,
                "Show Description Separator", VshSettingIcon.ICON_ANDROID,
                { setSeparatorLine() },
                { VshView.descriptionSeparator.toLocalizedString() },
                {
                    VshY.VshOptionsBuilder()
                        .add(y){ setSeparatorLine(true) }
                        .add(n){ setSeparatorLine(false) }
                        .build()
                }
            )
        )

        val modding = VshSettingCategory(0x1975, "Modding", "", transparentIcon)
        settings.addContent(modding)

        modding.add(
            VshSettingIcon(
                0xd1808, this,
                getString(R.string.setting_gameboot_custom_guide), VshSettingIcon.ICON_ANDROID,
                { launchURL("https://github.com/EmiyaSyahriel/CrossLauncher#animation-modding" )},
                { getString(R.string.common_click_here) }
            )
        )

        val home = vsh.findById("HOME")

        home?.addContent(
            VshSettingIcon(
                0xd18035, this,
                getString(R.string.app_hide_menu), VshSettingIcon.ICON_START,
                {vsh.hideMenu = !vsh.hideMenu},
                {getString(R.string.app_hide_menu_desc)}
            )
        )

        home?.items?.add(
            VshSettingIcon(
                0xd18035, this,
                getString(R.string.menu_rebuild_db), VshSettingIcon.ICON_REFRESH,
                {switchToRefreshRequestWindow() },
                { getString(R.string.menu_rebuild_db_desc) }
            )
        )
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
        alert.iconBitmap = resources.getDrawable(R.drawable.icon_refresh).toBitmap(vsh.d(32),vsh.d(32))
        setContentView(alert)
    }

    private val orientationName = mapOf(
        Pair(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, R.string.orient_portrait),
        Pair(ActivityInfo.SCREEN_ORIENTATION_USER, R.string.orient_user),
        Pair(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, R.string.orient_landscape)
    )

    private fun setOperatorName() {
        val telephonyManager = getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        vsh.operatorName = telephonyManager.simOperatorName
        vsh.use24Format = android.text.format.DateFormat.is24HourFormat(this)
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
        vsh.sendBackSignal()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(!isOnMenu) return false
        var retval = false

        when(keyCode){
            KeyEvent.KEYCODE_DPAD_UP -> {
                vsh.setSelection(0,-1)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                vsh.setSelection(0,1)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                vsh.setSelection(-1,0)
                retval = true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT ->{
                vsh.setSelection(1,0)
                retval = true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, mimickedConfirmButton ->{
                if(vsh.isOnOptions){
                    vsh.executeCurrentOptionItem()
                }else{
                    vsh.executeCurrentItem()
                }
                retval = true
            }
            KeyEvent.KEYCODE_DEL, mimickedCancelButton ->{
                if(vsh.isOnOptions){
                    vsh.switchOptionPopupVisibility()
                }else{
                    vsh.hideMenu = !vsh.hideMenu
                }
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_BUTTON_Y -> {
                vsh.switchOptionPopupVisibility()
                retval = true
            }
        }

        if(event != null){
            val c = event.unicodeChar.toChar()
            if(!vsh.hideMenu){
                try{
                    val data =  vsh.category[vsh.selectedX].items.find { it.name.toLowerCase(Locale.getDefault()).startsWith(c.toLowerCase())}
                    if(data != null){
                        var yIndex = vsh.category[vsh.selectedX].items.indexOf (data)
                        if(yIndex < 0) yIndex = vsh.selectedY
                        vsh.setSelectionAbs(vsh.selectedX, yIndex)
                    }
                }catch (e : Exception){}
            }
        }
        return retval || super.onKeyDown(keyCode, event)
    }

    private fun loadApps(){
        val apps = vsh.findById("APPS") ?: return
        val games = vsh.findById("GAME") ?: return
        apps.items.clear()
        games.items.clear()
        vsh.clockAsLoadingIndicator = true

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resInfo = packageManager.queryIntentActivities(intent, 0)

        resInfo.forEachIndexed { index, it ->
            if(!appListerThread.isInterrupted){
                val isGame = packageIsGame(it.activityInfo)
                //println("VTX_Activity [I] | New App : ${appData.name} (${appData.pkg}) - isGame : $isGame")
                //val size = File(it.activityInfo.applicationInfo.sourceDir).length().toSize()
                val size = it.activityInfo.packageName
                val xmbData = AppIcon(this, 0xc00 + index, it)

                // Filter itself
                if(it.activityInfo.packageName != packageName){
                    (if(isGame){games}else{apps}).items.add(xmbData)
                }

            }
        }

        apps.items.sortBy { it.name }
        games.items.sortBy { it.name }

        vsh.clockAsLoadingIndicator = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            // Reload when uninstall is requested
            UNINSTALL_REQ_CODE -> if(resultCode == RESULT_OK) runOnOtherThread { loadApps() }
        }
    }

    fun startApp(packageName: String){
        vsh.setOptionPopupVisibility(false)
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

    private fun loadAudio(){
        val music = vsh.findById("SONG") ?: return
        music.items.clear()
        SongIcon.songList.clear()

        vsh.clockAsLoadingIndicator = true
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = musicResolver.query(musicUri, null, null, null, null)

        var index = 0
        if(cursor != null && cursor.moveToFirst()){
            val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val pathCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            do{
                val id = cursor.getLong(idCol)
                val path = cursor.getString(pathCol)
                val item = SongIcon(id.toInt(), path, this)
                music.items.add(item)
                index ++
            }while(cursor.moveToNext())
        }
        cursor?.close()
        vsh.clockAsLoadingIndicator = false
    }

    // TODO: direct this to XMB Audio Player Service instead of internal sfx player
    fun openAudioFile(metadata:SongIcon.SongMetadata) {

    }

    private fun loadVideo(){
        vsh.clockAsLoadingIndicator = true
        val vids = vsh.findById("FILM") ?: return
        val videoResolver = contentResolver
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = videoResolver.query(videoUri, null,null,null,null)

        var index = 0
        if(cursor != null && cursor.moveToFirst()){
            val idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID)
            do{
                val id = cursor.getLong(idCol)
                val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
                val path = cursor.getString(dataCol)
                val item = VideoIcon(id.toInt(), this, path)
                vids.items.add(item)
                index++
            }while(cursor.moveToNext())
        }

        cursor?.close()
        vsh.clockAsLoadingIndicator = false
    }

    private fun getUriForFile(path: String):Uri{
        var filePath = path
        if(filePath.startsWith("//")){
            filePath = filePath.substring(2)
        }
        return Uri.parse("content://id.psw.vshlauncher.fileprovider/all").buildUpon().appendPath(filePath).build()
    }

    fun openVideoFile(file:File){
        val uri = Uri.fromFile(file)
        CurrentAppData.selectedVideoPath = file.path
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        Log.d(TAG, "Opening video V/MX - $uri ($mime)")
        //grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(this, XMBVideoPlayer::class.java).apply {
            data = uri
            type = mime
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION )
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        try{
            startActivity(intent)
            overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
        }catch (e: Exception){
            Toast.makeText(this, "This video type is not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overridePendingTransition(R.anim.anim_ps3_zoomfadein, R.anim.anim_ps3_zoomfadeout)
    }

    private fun getMime(path:String) : String{
        val type = getFileExtension(path)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
            ?: MimeTypeDict.maps[type.toLowerCase(Locale.ROOT)]
            ?: "*/*"
    }

    private fun getFileExtension(path:String) : String{ return path.split(".").last() }

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
        val pivotY = (vsh.height * 0.3f) + vsh.d(75f)
        val pivotX = vsh.width * 0.3f
        launchArea.top = pivotY - vsh.d(40)
        launchArea.left = pivotX - vsh.d(40)
        launchArea.bottom = pivotY + vsh.d(40)
        launchArea.right = pivotX + vsh.d(40)
        VshView.launchTapArea = launchArea
    }

    private var touchCount = 0
    private var lastTouchTime = 0L
    private var inputTimer = Timer()
    private fun onTouchCountChange(now:Int, last:Int, timeDelta:Long){
        // Switch vsh option when two tap is detected and less than 0.2s difference
        if(last < now && now == 2 && timeDelta < 100L){
            vsh.switchOptionPopupVisibility()
        }

        // Reset touch count after a second
        inputTimer.schedule(1000L){ touchCount = 0 }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isOnMenu) return false

        var retval = false
        val lastTouchCount = touchCount
        val touchTime = System.currentTimeMillis()
        val timeDelta = touchTime - lastTouchTime

        when(event.actionMasked){
            MotionEvent.ACTION_UP, MotionEvent.ACTION_HOVER_EXIT, MotionEvent.ACTION_POINTER_UP ->{
                isDrag = false
                directionLock = DIRLOCK_NONE

                touchCount --

                onTouchCountChange(touchCount, lastTouchCount, timeDelta)
                lastTouchTime = touchTime

                if(touchCurrentPoint.distanceTo(touchStartPoint) < touchSlop){

                    if(vsh.hideMenu) {
                        // Unhide on tap
                        vsh.hideMenu = false
                    }
                    recalculateChooseRect()

                    if(vsh.isOnOptions){
                        if(touchStartPoint in VshView.optionLaunchArea && !vsh.hideMenu){
                            vsh.executeCurrentOptionItem()
                        }
                    }else{
                        if(touchStartPoint in launchArea && !vsh.hideMenu){
                            vsh.executeCurrentItem()
                        }
                    }
                }
                directionLock = DIRLOCK_NONE
                retval = true
            }
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_HOVER_ENTER, MotionEvent.ACTION_POINTER_DOWN -> {
                touchStartPoint.x = event.x
                touchStartPoint.y = event.y
                touchCurrentPoint.x = event.x
                touchCurrentPoint.y = event.y
                touchDeltaStartPoint.x = event.x
                touchDeltaStartPoint.y = event.y
                isDrag = true
                retval = true

                touchCount ++
                onTouchCountChange(touchCount, lastTouchCount, timeDelta)
                lastTouchTime = touchTime
            }
            MotionEvent.ACTION_MOVE ->{
                touchCurrentPoint.set(event.x, event.y)
                val minMove = vsh.d(75f)
                if(isDrag){
                    val xLen = touchCurrentPoint.x - touchDeltaStartPoint.x
                    val yLen = touchCurrentPoint.y - touchDeltaStartPoint.y
                    if(kotlin.math.abs(xLen) > minMove && directionLock != DIRLOCK_VERTICAL ){
                        directionLock = DIRLOCK_HORIZONTAL
                        vsh.setSelection((xLen > 0).choose(-1,1),0)
                        touchDeltaStartPoint.set(touchCurrentPoint)
                    }else if(kotlin.math.abs(yLen) > minMove && directionLock != DIRLOCK_HORIZONTAL){
                        directionLock = DIRLOCK_VERTICAL
                        val yDir = if(vsh.isOnOptions) (yLen > 0).choose(1,-1) else (yLen > 0).choose(-1, 1)
                        vsh.setSelection(0,yDir)
                        touchDeltaStartPoint.set(touchCurrentPoint)
                    }
                    retval = true
                }
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
