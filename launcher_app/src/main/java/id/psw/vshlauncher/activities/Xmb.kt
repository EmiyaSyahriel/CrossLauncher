package id.psw.vshlauncher.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.PadKey
import id.psw.vshlauncher.types.items.XmbAppItem
import id.psw.vshlauncher.views.M
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.dialogviews.InstallPackageDialogView
import id.psw.vshlauncher.views.dialogviews.UITestDialogView
import kotlin.math.abs

class Xmb : AppCompatActivity() {

    companion object{
        const val ACT_REQ_UNINSTALL = 0xDACED0
        const val INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    }
    lateinit var xmbView : XmbView
    var skipColdBoot = false
    var sysBarVisibility = SysBar.NONE

    private var _lastOrientation : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readPreferences()
        XmbAppItem.showHiddenByConfig = false // To make sure

        if(vsh.useInternalWave){
            setContentView(R.layout.layout_xmb)
            xmbView = findViewById(R.id.xmb_view)
        }else{
            xmbView = XmbView(this)
            setContentView(xmbView)
        }
        vsh.xmbView = xmbView
        readXmbViewPreference()

        sysBarTranslucent()
        updateSystemBarVisibility()
        vsh.mediaListingStart()

        vsh.doMemoryInfoGrab = true
        handleAdditionalIntent(intent)
    }

    @Suppress("DEPRECATION") // We need to support down to Android API 19 (KitKat)
    fun updateSystemBarVisibility(){

        var flag =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        if(!(sysBarVisibility hasFlag SysBar.NAVIGATION)){
            flag = flag or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        if(!(sysBarVisibility hasFlag SysBar.STATUS)){
            flag = flag or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        if(sysBarVisibility == SysBar.NONE){
            flag = flag or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        window.decorView.systemUiVisibility = flag

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if(hasFocus){
            sysBarTranslucent()
            updateSystemBarVisibility()
        }
    }

    private fun handleAdditionalIntent(intent: Intent){
        when {
            isCreateShortcutIntent(intent) -> {
                showShortcutCreationDialog(intent)
            }
            intent.isShareIntent -> {
                showShareIntentDialog(intent)
            }
            vsh.isXPKGIntent(intent) -> {
                vsh.showInstallPkgDialog(intent)
            }
            intent.action == Consts.ACTION_WAVE_SETTINGS_WIZARD -> {
                vsh.showXMBLiveWallpaperWizard()
            }
            intent.action == Consts.ACTION_UI_TEST_DIALOG -> {
                xmbView.showDialog(UITestDialogView(xmbView))
            }
        }

        checkIsDefaultHomeIntent()
    }

    private fun checkIsDefaultHomeIntent() {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        val ri = if(sdkAtLeast(33))
            packageManager.resolveActivity(i, PackageManager.ResolveInfoFlags.of(0))
            else packageManager.resolveActivity(i, 0)
        vsh.shouldShowExitOption = ri?.activityInfo?.packageName != packageName
    }

    override fun onNewIntent(intent: Intent?) {
        if(intent != null) handleAdditionalIntent(intent)
        super.onNewIntent(intent)
    }

    private fun readPreferences() {
        val pref = M.pref
        requestedOrientation = pref.get(PrefEntry.DISPLAY_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR)
        PadKey.spotMarkedByX = pref.get(PrefEntry.CONFIRM_BUTTON, 0) == 1
        vsh.useInternalWave = pref.get(PrefEntry.USES_INTERNAL_WAVE_LAYER, false)
        sysBarVisibility = pref.get(PrefEntry.SYSTEM_STATUS_BAR, SysBar.ALL)
    }

    private fun readXmbViewPreference(){
        xmbView.fpsLimit = M.pref.get(PrefEntry.SURFACEVIEW_FPS_LIMIT, 0).toLong()
    }

    private fun sysBarTranslucent(){
        //if(Build.VERSION.SDK_INT >= 19){
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
        //}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            ACT_REQ_UNINSTALL -> {
                if(resultCode == RESULT_OK){
                    vsh.postNotification(null, getString(R.string.app_uninstall),getString(R.string.app_refresh_due_to_uninstall))
                    vsh.reloadAppList()
                }
            }
            Vsh.ACT_REQ_INSTALL_PACKAGE -> {
                if(resultCode == RESULT_OK){
                    if(data != null){
                        xmbView.showDialog(InstallPackageDialogView(xmbView, data))
                    }else{
                        vsh.postNotification(
                            R.drawable.ic_folder,
                            getString(R.string.error_common_header),
                            getString(R.string.custom_package_error_no_intent),
                            3.0f)
                    }
                }
            }
            Vsh.ACT_REQ_MEDIA_LISTING -> {
                if(resultCode == RESULT_OK){
                    vsh.mediaListingStart()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        M.audio.removeAudioSource()
        M.audio.preventPlayMedia = true
        xmbView.pauseRendering()
        vsh.doMemoryInfoGrab = false
        super.onPause()
    }

    override fun onResume() {
        vsh.xmbView = xmbView
        xmbView.startDrawThread()
        vsh.doMemoryInfoGrab = true
        super.onResume()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            arrayOf(
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RX,
                MotionEvent.AXIS_RY,
                MotionEvent.AXIS_RZ,
            ).forEach {
                val value = event.getAxisValue(it)
                if(abs(value) > 0.1f){
                    val k = M.gamepad.translateAxis(it, value, event.device.vendorId, event.device.productId)
                    if(k != PadKey.None){
                        retval = xmbView.onGamepadInput(k, event.actionMasked == MotionEvent.ACTION_DOWN)
                    }
                }
            }
        }
        return retval || super.onGenericMotionEvent(event)
    }

    val touchStartPointF = PointF()
    val touchCurrentPointF = PointF()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            val sc = xmbView.scaling
            val x = (event.x / sc.fitScale) + sc.viewport.left
            val y = (event.y / sc.fitScale) + sc.viewport.top
            if(event.action == MotionEvent.ACTION_DOWN){
                retval = true
                touchStartPointF.set(x, y)
            }
            touchCurrentPointF.set(x, y)

            xmbView.onTouchScreen(touchStartPointF, touchCurrentPointF, event.action)
        }
        return retval || super.onTouchEvent(event)
    }

    override fun onBackPressed() {

        // Do not call super.onBackPressed() which causing the app to close
        // Substitute this with Escape Button

        val k = M.gamepad.translate(KeyEvent.KEYCODE_ESCAPE, 0)
        xmbView.onGamepadInput(k, true)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            val key = M.gamepad.translate(keyCode, event.device.vendorId, event.device.productId)
            if(key != PadKey.None){
                retval = xmbView.onGamepadInput(key, false)
            }
        }

        return retval || super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            val key = M.gamepad.translate(keyCode, event.device.vendorId, event.device.productId)
            if(key != PadKey.None){
                retval = xmbView.onGamepadInput(key, true)
            }
        }

        return retval ||  super.onKeyDown(keyCode, event)
    }

    fun appOpenInPlayStore(pkgName:String){
        try{
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=$pkgName")))
        }catch(e:ActivityNotFoundException){
            vsh.postNotification(null, getString(R.string.error_no_appmarket_title),getString(R.string.error_no_appmarket_description))
        }catch(e:Exception){
            vsh.postNotification(null, getString(R.string.error_fail_to_find_at_market_title),getString(R.string.error_fail_to_find_at_market_desc))
        }
    }

    fun appRequestUninstall(pkgName:String){
        val permissionAllowed = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.REQUEST_DELETE_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
        if(permissionAllowed){
            val i = Intent(Intent.ACTION_UNINSTALL_PACKAGE).setData(Uri.parse("package:$pkgName"))
            startActivityForResult(i, ACT_REQ_UNINSTALL)
        }
    }

}