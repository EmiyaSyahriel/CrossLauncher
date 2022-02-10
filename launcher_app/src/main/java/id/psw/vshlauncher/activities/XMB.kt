package id.psw.vshlauncher.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import id.psw.vshlauncher.*
import id.psw.vshlauncher.views.XmbView

class XMB : AppCompatActivity() {

    companion object{
        const val ACT_REQ_UNINSTALL = 0xDACED0
        const val INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
    }
    private lateinit var xmbView : XmbView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        xmbView = XmbView(this)
        sysBarTranslucent()
        setContentView(xmbView)
        vsh.xmbView = xmbView
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
        super.onPause()
    }

    override fun onResume() {
        vsh.xmbView = xmbView
        super.onResume()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Gamepad.motionEventReceiver(event)
        }
        return retval || super.onGenericMotionEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Gamepad.touchEventReceiver(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Gamepad.keyEventReceiver(false, keyCode, event)
        }
        return retval || super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Gamepad.keyEventReceiver(true, keyCode, event)
        }
        if(keyCode == KeyEvent.KEYCODE_SLASH
        ) swapLayoutType = true
        if(keyCode == KeyEvent.KEYCODE_C){
            val handle  = vsh.addLoadHandle()
            Handler(Looper.getMainLooper()).postDelayed({ vsh.setLoadingFinished(handle) }, 1500L)
        }
        return retval ||  super.onKeyDown(keyCode, event)
    }

    fun appOpenInPlayStore(pkgName:String){
        try{
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=$pkgName")))
        }catch(e:PackageManager.NameNotFoundException){
            vsh.postNotification(null, getString(R.string.error_no_appmarket_title),getString(R.string.error_no_appmarket_description))
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