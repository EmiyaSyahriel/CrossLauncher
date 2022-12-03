package id.psw.vshlauncher.activities

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
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
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.core.content.ContextCompat
import id.psw.vshlauncher.*
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.dialogviews.UITestDialogView
import id.psw.vshlauncher.views.showDialog
import kotlin.math.abs

class XMB : AppCompatActivity() {

    companion object{
        const val ACT_REQ_UNINSTALL = 0xDACED0
        const val INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    }
    lateinit var xmbView : XmbView
    var skipColdBoot = false

    private var _lastOrientation : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readPreferences()
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

        xmbView.switchPage(skipColdBoot.select(VshViewPage.MainMenu, VshViewPage.ColdBoot))

        _lastOrientation = resources.configuration.orientation

        checkCanvasHwAcceleration()

        if(_lastOrientation == Configuration.ORIENTATION_PORTRAIT){
            postPortraitScreenOrientationWarning()
        }
        vsh.doMemoryInfoGrab = true
        handleAdditionalIntent(intent)
    }

    private fun handleAdditionalIntent(intent: Intent){
        when {
            isCreateShortcutIntent(intent) -> {
                showShortcutCreationDialog(intent)
            }
            isShareIntent(intent) -> {
                showShareIntentDialog(intent)
            }
            intent.action == Consts.ACTION_WAVE_SETTINGS_WIZARD -> {
                vsh.showXMBLiveWallpaperWizard()
            }
            intent.action == Consts.ACTION_UI_TEST_DIALOG -> {
                xmbView.showDialog(UITestDialogView(vsh))
            }
        }

        checkIsDefaultHomeIntent(intent)
    }

    private fun checkIsDefaultHomeIntent(intent: Intent) {
        vsh.shouldShowExitOption = !intent.hasCategory(Intent.CATEGORY_DEFAULT) && intent.hasCategory(Intent.CATEGORY_HOME)
    }

    override fun onNewIntent(intent: Intent?) {
        if(intent != null) handleAdditionalIntent(intent)
        super.onNewIntent(intent)
    }

    private fun readPreferences() {
        val pref = vsh.pref
        requestedOrientation = pref.getInt(PrefEntry.DISPLAY_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_SENSOR)
        GamepadSubmodule.Key.spotMarkedByX = pref.getInt(PrefEntry.CONFIRM_BUTTON, 0) == 1
        vsh.useInternalWave = pref.getBoolean(PrefEntry.USES_INTERNAL_WAVE_LAYER, false)
    }

    private fun readXmbViewPreference(){
        xmbView.fpsLimit = vsh.pref.getInt(PrefEntry.SURFACEVIEW_FPS_LIMIT, 0).toLong()
    }

    private fun checkCanvasHwAcceleration(){
        if(!xmbView.isHWAccelerated){
            vsh.postNotification(null, getString(R.string.no_hwaccel_warning_title),
                getString(R.string.no_hwaccel_warning_desc)
            )
        }
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
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        vsh.removeAudioSource()
        vsh.preventPlayMedia = true
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
                MotionEvent.AXIS_HAT_X,
                MotionEvent.AXIS_HAT_Y,
                MotionEvent.AXIS_RX,
                MotionEvent.AXIS_RY,
                MotionEvent.AXIS_RZ,
            ).forEach {
                val value = event.getAxisValue(it)
                if(abs(value) > 0.1f){
                    val k = vsh._gamepad.translateAxis(it, value, event.device.vendorId, event.device.productId)
                    if(k != GamepadSubmodule.Key.None){
                        retval = xmbView.onGamepadInput(k, true)
                    }
                }
            }
        }
        return retval || super.onGenericMotionEvent(event)
    }

    val touchStartPointF = PointF()
    val touchCurrentPointF = PointF()

    private var screenOrientationWarningPosted = false
    private fun postPortraitScreenOrientationWarning() {
        if(!screenOrientationWarningPosted){
            vsh.postNotification(null,
                getString(R.string.screen_portrait_warning_title),
                getString(R.string.screen_portrait_warning_desc)
            )
            screenOrientationWarningPosted = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if(newConfig.orientation != _lastOrientation){
            when(newConfig.orientation){
                Configuration.ORIENTATION_PORTRAIT ->
                {
                    postPortraitScreenOrientationWarning()
                }
                else-> {

                }
            }
            _lastOrientation = newConfig.orientation
        }
        super.onConfigurationChanged(newConfig)
    }

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

        val k = vsh._gamepad.translate(KeyEvent.KEYCODE_ESCAPE, 0)
        xmbView.onGamepadInput(k, true)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            val key = vsh._gamepad.translate(keyCode, event.device.vendorId, event.device.productId)
            if(key != GamepadSubmodule.Key.None){
                retval = xmbView.onGamepadInput(key, false)
            }
        }

        return retval || super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            val key = vsh._gamepad.translate(keyCode, event.device.vendorId, event.device.productId)
            if(key != GamepadSubmodule.Key.None){
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