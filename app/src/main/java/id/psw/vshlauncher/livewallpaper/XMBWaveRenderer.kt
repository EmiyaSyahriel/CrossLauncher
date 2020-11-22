package id.psw.vshlauncher.livewallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object XMBWaveRenderer {
    data class XMBScalingTransport(val w:Float, val h:Float, val density:Float)
    private data class Particle(val x:Float, val y:Float, val size:Float, val alpha:Float)

    var wavePaint = Paint().apply {
        color = Color.argb(32,255,255,255)
    }
    var particlePaint = Paint()

    var drawDynamicParticle = false
    private val paths = arrayListOf(Path(), Path(), Path(), Path(),Path(), Path())
    private val particlePath = Path()
    private val particles = arrayListOf<Particle>()
    // Taken from https://github.com/zanneth/Wavelike/blob/master/shaders/wave_vertex.glsl
    private fun waveFunction(x:Float, amp:Float, time:Float): Float{
        val x1 = amp * sin(x - time)
        val x2 = amp * cos(x - time)
        val x3 = cos(x / PI).pow(2.0).toFloat()
        return (x1 + x2 + x3) - 1.0f;
    }

    private fun updatePaths(w:Float, amp:Float, h:Float){
        paths.forEachIndexed {index, it ->
            it.reset()
            var i = 0
            var ms = System.currentTimeMillis().toFloat()
            // draw first line
            it.moveTo(0f,0f)
            while(i <= w){
                val iF = (i.toFloat() + (index * w)) * w
                it.lineTo(iF, waveFunction(iF, amp, ms))
                ms += w
                i++
            }
            while(i >= 0){
                val iF = (i.toFloat() + (index * w)) * w
                it.lineTo(iF, waveFunction(iF, amp, ms) * h)
                ms += w
                i--
            }
        }
    }

    private fun updateParticles(w:Float, h:Float){
        if(particles.size < 128){
            for(i in 0 .. 128){
                particles.add(Particle(0f,0f,0f,0f))
            }
        }


    }

    fun draw(canvas: Canvas, scaleData:XMBScalingTransport){
        updatePaths(scaleData.w, 0.2f, scaleData.w)
    }

}