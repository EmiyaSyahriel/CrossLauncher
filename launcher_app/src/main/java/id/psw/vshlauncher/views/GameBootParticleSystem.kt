package id.psw.vshlauncher.views

import android.graphics.*
import id.psw.vshlauncher.FColor
import id.psw.vshlauncher.toLerp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

class GameBootParticleSystem {
    data class Particle(
        var from : PointF = PointF(),
        var to : PointF = PointF(),
        var baseColor : Int = -1,
        var startSize : Float = 0.01f,
        var endSize : Float = 0.02f,
        var age : Float = 0.0f,
        var maxAge : Float = 2.0f
    ){
        val t : Float get() = age / maxAge
        val x : Float get() = t.toLerp(from.x, to.x)
        val y : Float get() = t.toLerp(from.y, to.y)
        val size get() = t.toLerp(startSize, endSize)
    }

    companion object {
        fun weighting(factor:Float):Float{
            return (1.0f - abs(sin(factor) * Math.PI.toFloat())).pow(2.0f)
        }

        private val cByteLookup = mapOf(0 to 0xFF, 1 to 0xBF, 2 to 0xCF, 3 to 0xDF, 4 to 0xEF, 5 to 0xAF)

        private val rc : Int get() = (cByteLookup[(rf * 5.0f).roundToInt().coerceIn(0,5)] ?: 0xFF).toInt().coerceAtMost(255)
        private val rf : Float get() = Math.random().toFloat()
    }

    var particles = arrayListOf<Particle>()
    var baseDuration = 2.0f

    fun init(count:Int = 128){
        for(i in 0 until 128){
            particles.add(reset(Particle()))
        }
    }

    fun reset(p:Particle):Particle{
        p.age = 0.0f
        p.maxAge = (Math.random() * 1.0f).toFloat().toLerp(0.25f, 1.0f)
        p.from.x = rf.toLerp(0.0f, 1.0f)
        p.from.y = rf.toLerp(0.45f, 0.55f)
        p.to.x = p.from.x + rf.toLerp(-0.1f, 0.1f) * weighting(p.from.x).toLerp(0.25f, 1.0f)
        p.to.y = p.from.y + rf.toLerp(-0.25f, 0.5f) * weighting(p.from.x).toLerp(0.25f, 1.0f)
        p.startSize = rf.toLerp(0.01f,0.02f) * weighting(p.from.x).toLerp(1.0f, 1.5f)
        p.endSize = rf.toLerp(0.025f,0.05f) * weighting(p.from.x).toLerp(1.0f, 1.5f)
        p.baseColor = Color.argb(255, rc,rc,rc)
        return p
    }

    fun update(deltaTime:Float) : ArrayList<Particle> {
        particles.forEach {
            it.age += deltaTime / baseDuration
            if(it.t > 1.0f){
                reset(it)
            }
        }
        return particles
    }
}