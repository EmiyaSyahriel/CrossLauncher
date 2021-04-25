package id.psw.vshlauncher.livewallpaper.ogl

import android.opengl.GLES20;
import java.nio.FloatBuffer
import kotlin.math.abs
import kotlin.math.max

data class Vector3(var x:Float, var y:Float, var z:Float = 0.0f){
    companion object {
        val zero : Vector3 get() = Vector3(0f,0f,0f)
        val one  : Vector3 get() = Vector3(1f,1f,1f)
    }

    fun appendTo(list : ArrayList<Float>){
        list.add(x)
        list.add(y)
        list.add(z)
    }

    fun appendTo(buffer: FloatBuffer, pos:Vector3, scl:Vector3){
        buffer.put((x* scl.x) + pos.x)
        buffer.put((y* scl.y) + pos.y)
        buffer.put((z* scl.z) + pos.z)
    }

    fun normalize()
    {
        val magn = magnitude()
        x /= magn
        y /= magn
        z /= magn
    }

    fun magnitude () : Float = max(max(abs(x), abs(y)), abs(z))
    infix operator fun times(a:Float) : Vector3 = Vector3(x * a, y * a, z * a)
    infix operator fun timesAssign(a:Float) { x *= a; y *= a; z*= a; }
    infix operator fun plus(a:Vector3) : Vector3 = Vector3(x + a.x, y + a.y, z + a.z)
    infix operator fun plusAssign(a:Float) { x += a; y += a; z += a; }
    infix operator fun minus(a:Vector3) : Vector3 = Vector3(x - a.x, y - a.y, z - a.z)
    infix operator fun minusAssign(a:Float) { x -= a; y -= a; z -= a; }

}

data class Vector2(var x:Float, var y:Float){
    companion object {
        val zero : Vector2 get() = Vector2(0f,0f)
        val one  : Vector2 get() = Vector2(1f,1f)
    }
    fun appendTo(list : ArrayList<Float>){
        list.add(x)
        list.add(y)
    }

    fun appendTo(buffer: FloatBuffer, pos:Vector2, scl:Vector2){
        buffer.put((x* scl.x) + pos.x)
        buffer.put((y* scl.y) + pos.y)
    }
}