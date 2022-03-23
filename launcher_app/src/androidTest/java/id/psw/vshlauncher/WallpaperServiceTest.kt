package id.psw.vshlauncher

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WallpaperServiceTest {
    @get:Rule
    public val wpChangePerm = GrantPermissionRule.grant(android.Manifest.permission.SET_WALLPAPER)

    companion object{
        const val TAG = "wpsvc.test"
    }

    @Test
    fun getActiveWallpaper(){
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val svc = context.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager?
        if(svc != null){
            if(!svc.isWallpaperSupported){
                Log.w(TAG, "Wallpaper is not supported in this device")
            }

            val winfo = svc.wallpaperInfo
            if(winfo != null){
                Log.d(TAG,"Using Live Wallpaper : ${winfo.serviceName}")
                return
            }

            val wdraw = svc.drawable
            if(wdraw != null){
                Log.d(TAG, "Using Static Wallpaper : ${wdraw.intrinsicWidth}x${wdraw.intrinsicHeight}")
                return
            }

            Log.w(TAG, "No Wallpaper is found!")
        }else{
            Log.w(TAG, "Wallpaper Manager didn't exists! Probably using TV device or using device that didn't have display.")
        }
    }

    @Test
    fun setWallpaper(){
        val wpfile = File("/storage/emulated/0/Pictures/WP.png")
        if(wpfile.exists()){
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext
            val wpman = ctx.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
            wpman.setStream(wpfile.inputStream())
        }else{
            Log.w(TAG, "Please put a wallpaper file at \"/storage/emulated/0/Pictures/WP.png\"")
        }
    }

    @Test
    fun setLiveWallpaper(){
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val i = Intent()
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val svcClass = id.psw.vshlauncher.livewallpaper.XMBWaveWallpaperService::class.java
        if(Build.VERSION.SDK_INT > 15){
            i.action = WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER
            val p = svcClass.`package`?.name ?: ""
            val c = svcClass.canonicalName
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(p,c))
        }else{
            i.action = WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
        }
        ctx.startActivity(i)
    }

    @Test
    fun assignInternalLiveWallpaperActive(){
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val pref = ctx.getSharedPreferences("xRegistry.sys", Context.MODE_PRIVATE)
        pref.edit()
            .putBoolean(PrefEntry.USES_INTERNAL_WAVE_LAYER, true)
            .apply()
        Log.d(TAG, "Assigning complete, restart CrossLauncher to apply change.")
    }
}