package id.psw.vshlauncher

import android.app.Service
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
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
import android.content.*
import android.content.res.Configuration
import android.graphics.*
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.contains
import java.io.File
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
        const val DIRLOCK_NONE = 0xab
        const val DIRLOCK_HORIZONTAL = 0xcd
        const val DIRLOCK_VERTICAL = 0xef
        const val PREF_ORIENTATION_KEY = "xmb_orientation"
        const val PREF_MIMIC_USA_CONSOLE = "xmb_PS3_FAT_CECHA00"
        const val PREF_USE_GAMEBOOT = "xmb_USE_GAMEBOOT"
        const val PREF_DYNAMIC_TWINKLE = "xmb_DYNAMIC_P3T"
        const val PREF_IS_FIRST_RUN = "xmb_not_new_user"
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
               // prefs.edit().putBoolean(PREF_IS_FIRST_RUN, false).apply()
            }else{
                setContentView(vsh)
            }
        }

        setContentView(coldboot)
        playColdbootSound()

        setOperatorName()

        checkPermission()
        appListerThread = Thread(Runnable {
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
        vsh.mediaPlayer = sfxPlayer
    }

    private fun loadPrefs(){
        xMarksTheSpot = prefs.getBoolean(PREF_MIMIC_USA_CONSOLE, false)
        scrOrientation = prefs.getInt(PREF_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        useGameBoot = prefs.getBoolean(PREF_USE_GAMEBOOT, true)
        dynamicThemeTwinkles = prefs.getBoolean(PREF_DYNAMIC_TWINKLE, true)
    }

    private fun checkPermission(){
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

    @Suppress("DEPRECATION")
    private fun populateSettingSections(){
        val settings = vsh.findById("SETT") ?: return
        settings.items.add(VshY(
            0xd1802,
            getString(R.string.item_orientation),
            getString(R.string.orient_landscape),
            resources.getDrawable(R.drawable.icon_orientation),
            vsh.density,
            Runnable {
                val item = settings.getItemBy(0xd1802) ?: return@Runnable
                when(scrOrientation){
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE -> {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, item)
                    }
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT -> {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER, item)
                    }
                    ActivityInfo.SCREEN_ORIENTATION_USER -> {
                        setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, item)
                    }
                }
                requestedOrientation = scrOrientation
            },
            arrayListOf(
                VshY.VshOptions().apply { name = getString(R.string.orient_landscape); onClick = Runnable {setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE, settings.getItemBy(0xd1802))}},
                VshY.VshOptions().apply { name = getString(R.string.orient_portrait); onClick = Runnable {setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT, settings.getItemBy(0xd1802))}},
                VshY.VshOptions().apply { name = getString(R.string.orient_user); onClick = Runnable {setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER, settings.getItemBy(0xd1802))}}
            )
        ))
        setOrientation(prefs.getInt(PREF_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE), settings.getItemBy(0xd1802))
        val useGameBootConfig = VshY(
            0xd1803,
            getString(R.string.setting_show_gameboot),
            "Yes",
            resources.getDrawable(R.drawable.icon_android),
            vsh.density
        )

        useGameBootConfig.onClick =
            Runnable{
                useGameBoot = !useGameBoot
                prefs.edit().putBoolean(PREF_USE_GAMEBOOT, useGameBoot).apply()
                useGameBootConfig.subtext = useGameBoot.choose("Yes","No")
            }
        useGameBootConfig.subtext = useGameBoot.choose("Yes","No")

        settings.items.add(useGameBootConfig)

        val dynamicThemeSetting = VshY(
            0xd1804,
            getString(R.string.setting_mimic_dynamic_theme),"Yes",
            resources.getDrawable(R.drawable.icon_dynamic_theme_effect),
            vsh.density
        )

        dynamicThemeSetting.onClick = Runnable {
            dynamicThemeTwinkles = !dynamicThemeTwinkles
            prefs.edit().putBoolean(PREF_DYNAMIC_TWINKLE, useGameBoot).apply()
            dynamicThemeSetting.subtext = dynamicThemeTwinkles.choose("Yes","No")
        }
        dynamicThemeSetting.subtext = dynamicThemeTwinkles.choose("Yes","No")

        settings.items.add(dynamicThemeSetting)

        settings.items.add(
            VshY(
            0xd18034,
                getString(R.string.setting_gameboot_custom_guide),
                getString(R.string.click_here),
                resources.getDrawable(R.drawable.icon_android),
                vsh.density
            )
        )
        settings.items.add(
            VshY(
                0xd18034,
                getString(R.string.setting_coldboot_custom_guide),
                getString(R.string.click_here),
                resources.getDrawable(R.drawable.icon_android),
                vsh.density
            )
        )

        val home = vsh.findById("HOME")

        home?.items?.add(
            VshY(
                0xd18035,
                "Rebuild App and Game Database",
                "in case new app is installed, but this app does not automatically show them.",
                resources.getDrawable(R.drawable.icon_refresh),
                vsh.density,
                onClick = Runnable {switchToRefreshRequestWindow()}
            )
        )
    }

    private fun switchToRefreshRequestWindow(){
        val alert = VshDialogView(this)
        alert.buttons.clear()
        alert.buttons.add(VshDialogView.Button("OK", Runnable {
            val thread = Thread(Runnable { loadApps() })
            thread.start()
            setContentView(vsh)
        }))
        alert.buttons.add(VshDialogView.Button("Cancel",
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

    private fun setOrientation(orientation:Int, icon:VshY?){
        scrOrientation = orientation
        requestedOrientation = orientation
        icon?.subtext = getString(orientationName[orientation] ?: R.string.orient_unknown)
        prefs.edit().putInt(PREF_ORIENTATION_KEY, scrOrientation).apply()
    }

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
        if(vsh.isOnOptions){
            vsh.isOnOptions = false
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(!isOnMenu) return false
        var retval = false
        val confirmButton = xMarksTheSpot.choose(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_BUTTON_B)

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
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, confirmButton ->{
                vsh.executeCurrentItem()
                retval = true
            }
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_BUTTON_Y -> {
                vsh.isOnOptions = !vsh.isOnOptions
                retval = true
            }
        }

        if(event != null){
            val c = event.unicodeChar.toChar()
            if(!vsh.hideMenu){
                try{
                    val data =  vsh.category[vsh.selectedX].items.find { it.text.toLowerCase(Locale.getDefault()).startsWith(c.toLowerCase())}
                    if(data != null){
                        var yIndex = vsh.category[vsh.selectedX].items.indexOf (data)
                        if(yIndex < 0) yIndex = vsh.selectedY
                        vsh.setSelectionAbs(vsh.selectedX, yIndex)
                    }
                }catch (e:java.lang.Exception){}
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
                val xmbData = VshY(
                    0xc00 + index,
                    it.loadLabel(packageManager).toString(),
                    size,
                    it.loadIcon(packageManager),
                    density = vsh.density,
                    onClick = Runnable { startApp(it.activityInfo.packageName)
                    }
                )

                // Filter itself
                if(it.activityInfo.packageName != packageName){
                    (if(isGame){games}else{apps}).items.add(xmbData)
                }

            }
        }

        apps.items.sortBy { it.text }
        games.items.sortBy { it.text }

        vsh.clockAsLoadingIndicator = false
    }

    private fun uninstallApp(packageName: String){
        val intent = Intent(Intent.ACTION_DELETE, Uri.fromParts("package", packageName, null))
        startActivity(intent)
        Timer("Uninstaller", true).schedule(5000){
            Thread(Runnable {loadApps()}).start()
        }
    }

    private fun startApp(packageName: String){
        vsh.mpShow = false
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
            v.setButton(arrayListOf(VshDialogView.Button(
                getString(android.R.string.ok),
                Runnable {
                    setContentView(vsh)
                }
            )
            ))
            v.contentText = "Cannot start this application.\n(NameNotFoundException : Not Installed)"
            v.titleText = "Launch Error"
            setContentView(v)
        }
    }

    override fun onResume() {
        super.onResume()
        if(returnFromGameboot){
            setContentView(vsh)
        }
    }

    fun reloadContent(){
        appListerThread.start()
    }

    private fun loadAudio(){
        val music = vsh.findById("SONG") ?: return
        music.items.clear()

        music.items.add(
            VshY(0x8080,getString(R.string.audioplayer_pause),"",
                resources.getDrawable(R.drawable.icon_player_pause),
                vsh.density,
                Runnable { sfxPlayer?.pause() }))

        music.items.add(
            VshY(0x8080,getString(R.string.audioplayer_resume),"",
                resources.getDrawable(R.drawable.icon_player_play),
                vsh.density,
                Runnable { sfxPlayer?.start() }))

        music.items.add(
            VshY(0x8080,getString(R.string.audioplayer_stop),"",
                resources.getDrawable(R.drawable.icon_player_stop),
                vsh.density,
                Runnable {
                    sfxPlayer?.stop()
                    sfxPlayer?.reset()
                    vsh.clockExpandInfo = ""
                }))

        vsh.clockAsLoadingIndicator = true
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = musicResolver.query(musicUri, null, null, null, null)

        var index = 0
        if(cursor != null && cursor.moveToFirst()){
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val pathCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            do{
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol) ?: getString(R.string.unknown)
                val path = cursor.getString(pathCol)
                val album = cursor.getString(albumCol) ?: getString(R.string.unknown)
                val albumArt = getAlbumArt(path)
                val item = VshY(id.toInt(), title, "$album - $artist" , albumArt, vsh.density, Runnable {
                    openAudioFile(path, title, artist, album, albumArt)
                })
                music.items.add(item)
                index ++
            }while(cursor.moveToNext())
        }
        cursor?.close()
        vsh.clockAsLoadingIndicator = false
    }

    private fun openAudioFile(path:String, title:String, artist:String, album:String, albumArt : Drawable) {
        if(sfxPlayer != null){
            vsh.mpShow = true
            sfxPlayer?.reset()
            sfxPlayer?.setDataSource(path)
            sfxPlayer?.prepare()
            vsh.mpAudioTitle = title
            vsh.mpAudioArtist = "$album / $artist"
            val size = (vsh.density * 70).toInt()
            vsh.mpAudioCover = albumArt.toBitmap(size,size)
            vsh.mpAudioFormat = getFileExtension(path).toUpperCase(Locale.ROOT)
        }
    }

    private fun loadVideo(){
        vsh.clockAsLoadingIndicator = true
        val vids = vsh.findById("FILM") ?: return
        val videoResolver = contentResolver
        val videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = videoResolver.query(videoUri, null,null,null,null)

        var index = 0
        if(cursor != null && cursor.moveToFirst()){
            val dataCol = cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            val idCol = cursor.getColumnIndex(MediaStore.Video.Media._ID)

            do{
                val id = cursor.getLong(idCol)
                val file = File(cursor.getString(dataCol))
                val fileName = file.name
                val size = file.length().toSize()
                val albumArt = ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND) ?: VshX.TransparentBitmap

                val item = VshY(id.toInt(), fileName, size, BitmapDrawable(albumArt), vsh.density, Runnable {
                    val uri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                    openVideoFile(file)
                })
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

    private fun openVideoFile(file:File){
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
        }catch (e:java.lang.Exception){
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

    private fun getAlbumArt(path:String) : Drawable {
        var retval = resources.getDrawable(R.drawable.icon_cda).toBitmap(128,128)
        try{
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(this, Uri.parse(path))
            val data = mmr.embeddedPicture
            if(data != null) retval = BitmapFactory.decodeByteArray(data, 0, data.size)
            mmr.release()
        }catch(e:Exception){
        }
        return BitmapDrawable(resources, retval)
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
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(!isOnMenu) return false

        var retval = false

        if(event.pointerCount == 2){
            if(event.actionMasked == MotionEvent.ACTION_UP){
                if(!vsh.hideMenu){
                    vsh.isOnOptions = !vsh.isOnOptions
                }
            }
        }else{
            when(event.actionMasked){
                MotionEvent.ACTION_UP, MotionEvent.ACTION_HOVER_EXIT, MotionEvent.ACTION_POINTER_UP ->{
                    isDrag = false
                    directionLock = DIRLOCK_NONE


                    if(touchCurrentPoint.distanceTo(touchStartPoint) < touchSlop){

                        if(vsh.hideMenu) {
                            // Unhide on tap
                            vsh.hideMenu = false
                        }
                        recalculateChooseRect()

                        if(touchStartPoint in launchArea && !vsh.hideMenu){
                            vsh.executeCurrentItem()
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
                            vsh.setSelection(0,(yLen > 0).choose(-1,1))
                            touchDeltaStartPoint.set(touchCurrentPoint)
                        }
                        retval = true
                    }
                }
            }
        }
        return retval || super.onTouchEvent(event)
    }

    override fun onDialogBack() {
        setContentView(vsh)
    }

}
