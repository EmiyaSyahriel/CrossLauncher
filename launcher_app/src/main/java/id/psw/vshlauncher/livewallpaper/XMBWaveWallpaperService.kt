package id.psw.vshlauncher.livewallpaper

import android.content.Intent
import android.opengl.GLSurfaceView
import android.util.Log
import com.learnopengles.android.switchinglivewallpaper.GLWallpaperService
import com.learnopengles.android.switchinglivewallpaper.OpenGLES2WallpaperService

class XMBWaveWallpaperService : OpenGLES2WallpaperService() {

    private lateinit var lastWaveRenderer: XMBWaveRenderer

    override fun onCreate() {
        super.onCreate()
    }

    override fun getNewRenderer(): GLSurfaceView.Renderer {
        lastWaveRenderer = XMBWaveRenderer()
        return lastWaveRenderer
    }
}