package id.psw.vshlauncher.test_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.psw.vshlauncher.R
import id.psw.vshlauncher.livewallpaper.NativeGL

class XMBWaveTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NativeGL.setAssetManager(assets)
        setContentView(R.layout.layout_xmbwave)
    }
}