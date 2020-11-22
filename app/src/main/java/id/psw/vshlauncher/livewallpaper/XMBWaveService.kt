package id.psw.vshlauncher.livewallpaper

import android.graphics.*
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class XMBWaveService : WallpaperService()
{
    override fun onCreateEngine(): Engine {
        return XMBWaveEngine()
    }


    inner class XMBWaveEngine() : Engine() {
        val handler = Handler()
        val doDraw = Runnable { draw() }
        val screenSize = Point(0,0)

        val linesPaint = Paint().apply{
            color = Color.argb(32,255,255,255)
            isAntiAlias = true
        }

        val particlePaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        init {
            handler.post(doDraw)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            screenSize.x = width
            screenSize.y = height
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            handler.removeCallbacks(doDraw)
        }

        val xmbLinePath = arrayListOf(Path(),Path(),Path(),Path(),Path(),Path())
        private fun updateLines(){

        }

        private fun drawBackground(canvas:Canvas){

        }

        private fun drawWaves(canvas:Canvas){
            updateLines()
            xmbLinePath.forEach {
                canvas.drawPath(it, linesPaint)
            }
        }

        private fun drawParticles(){

        }

        private fun draw(){
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try{
                canvas = holder.lockCanvas()
                if(canvas != null){
                    drawBackground(canvas)
                }
            }finally {
                if(canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            handler.removeCallbacks(doDraw)
            if(isVisible){
                handler.postDelayed(doDraw, 16)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if(visible){
                handler.post(doDraw)
            }else{
                handler.removeCallbacks(doDraw)
            }
        }

    }

}