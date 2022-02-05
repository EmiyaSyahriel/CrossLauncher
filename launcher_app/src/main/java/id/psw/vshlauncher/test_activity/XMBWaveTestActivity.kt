package id.psw.vshlauncher.test_activity

import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import id.psw.vshlauncher.R
import id.psw.vshlauncher.livewallpaper.NativeGL

class XMBWaveTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_xmbwave)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_SLASH){
            try{
                val i = Intent().setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                startActivity(i)
                return true
            }catch(e:PackageManager.NameNotFoundException){
                Toast.makeText(this, "Device has no live wallpaper manager...", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}