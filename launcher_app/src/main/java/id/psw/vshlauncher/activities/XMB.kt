package id.psw.vshlauncher.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import id.psw.vshlauncher.*
import id.psw.vshlauncher.views.VshView
import java.lang.Exception
import java.lang.IllegalStateException

class XMB : AppCompatActivity() {

    private lateinit var vshView : VshView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vshView = VshView(this)
        sysBarTranslucent()
        setContentView(vshView)
        vsh.vshView = vshView
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

    override fun onPause() {
        vsh.activeMediaPlayers.forEach {
        try{
            it.pause()
        }catch(ise:IllegalStateException){}
        }
        super.onPause()
    }

    override fun onResume() {
        vsh.vshView = vshView
        vsh.activeMediaPlayers.forEach {
            try{
                it.start()
            }catch(ise:IllegalStateException){}
        }
        super.onResume()
    }

    override fun onGenericMotionEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Input.motionEventReceiver(event)
        }
        return retval || super.onGenericMotionEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Input.touchEventReceiver(event)
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Input.keyEventReceiver(false, keyCode, event)
        }
        return retval || super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        var retval = false
        if(event != null){
            retval = VSH.Input.keyEventReceiver(true, keyCode, event)
        }
        if(keyCode == KeyEvent.KEYCODE_SLASH
        ) swapLayoutType = true
        if(keyCode == KeyEvent.KEYCODE_C){
            val handle  = vsh.addLoadHandle()
            Handler(Looper.getMainLooper()).postDelayed({ vsh.setLoadingFinished(handle) }, 1500L)
        }
        return retval ||  super.onKeyDown(keyCode, event)
    }

}