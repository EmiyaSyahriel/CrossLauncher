package id.psw.vshlauncher.livewallpaper

import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder

class XMBWaveWallpaperService : WallpaperService() {

    private lateinit var lastWaveRenderer: XMBWaveRenderer

    inner class WaveEngine(private val ctx: Context) : Engine(){
        private lateinit var surface : WaveSurface

        // Variant of the XMBWaveSurfaceView that uses surfaceHolder loop hack
        inner class WaveSurface : XMBWaveSurfaceView{

            constructor(context: Context?): super(context){
            }
            constructor(context: Context?, attrs: AttributeSet): super(context, attrs){
            }
            constructor(context:Context?, attrs: AttributeSet, styleSet: Int) : super(context, attrs){
            }

            override fun getHolder(): SurfaceHolder {
                return surfaceHolder
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            surface = WaveSurface(ctx)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if(visible) surface.onResume()
            else surface.onPause()
        }
    }

    override fun onCreateEngine(): Engine {
        return WaveEngine(this)
    }

}