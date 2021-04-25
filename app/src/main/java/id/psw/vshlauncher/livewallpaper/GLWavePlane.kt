package id.psw.vshlauncher.livewallpaper

import android.opengl.GLES20
import id.psw.vshlauncher.livewallpaper.ogl.GLShaders
import id.psw.vshlauncher.livewallpaper.ogl.GLShape
import id.psw.vshlauncher.livewallpaper.ogl.Vector2
import id.psw.vshlauncher.livewallpaper.ogl.Vector3
import kotlin.math.*

class GLWavePlane : GLShape() {
    override val renderUsage: Int = GLES20.GL_DYNAMIC_DRAW
    var currentTime = 0f

    companion object{
        const val MAX_X = 50f
        const val MAX_Y = 50f
    }

    init{
        name = "wave.mdl"
        shader = GLShaders.waveShader
        autoCalcNormal = true
    }

    private fun genWave(){
        currentTime = (System.currentTimeMillis() / 1000f) % 1000f
        vertices.clear()
        normals.clear()
        uv.clear()
        tris.clear()
        var x = 0
        var y =0
        while(y < MAX_X){
            while (x < MAX_Y){
                putFace(x,y)
                x++
            }
            y++
        }
    }

    private fun sin(a:Float) : Float = sin(a.toDouble()).toFloat()
    private fun cos(a:Float) : Float = cos(a.toDouble()).toFloat()
    private fun tan(a:Float) : Float = tan(a.toDouble()).toFloat()

    private fun putFace(xPos:Int, zPos:Int){
        val i = vertices.size
        val pos0 = Vector3(xPos / MAX_X, 0f, zPos / MAX_Y)
        val pos1 = Vector3(xPos / MAX_X, 0f,       (zPos + 1) / MAX_Y)
        val pos2 = Vector3((xPos + 1) / MAX_X, 0f, (zPos + 1) / MAX_Y)
        val pos3 = Vector3((xPos + 1) / MAX_X, 0f, zPos / MAX_Y)
        calcWaveY(pos0)
        calcWaveY(pos1)
        calcWaveY(pos2)
        calcWaveY(pos3)
        vertices.add(pos0)
        vertices.add(pos1)
        vertices.add(pos2)
        vertices.add(pos3)
        normals.add(Vector3.zero)
        normals.add(Vector3.zero)
        normals.add(Vector3.zero)
        normals.add(Vector3.zero)
        uv.add(Vector2.one)
        uv.add(Vector2.one)
        uv.add(Vector2.one)
        uv.add(Vector2.one)
        tris.add(i+3)
        tris.add(i+1)
        tris.add(i+0)
        tris.add(i+0)
        tris.add(i+1)
        tris.add(i+2)
    }

    private fun ndc(a:Float) : Float = (a * 2) - 1

    private fun calcWaveY(v:Vector3){
        v.x = ndc(v.x)
        v.z = ndc(0f)
        //v.y = ndc(sin(v.x + v.y + (currentTime * 1000f))) * 0.01f
        v.y = ndc(v.z)
    }

    override fun genBuffer() {
        genWave()
        super.genBuffer()
    }

    override fun render() {

        shader.setUniform("white",1f,1f,1f,1f)
        shader.setUniform("color",0f,1f,1f,0f)

        this.genBuffer()
        super.render()
    }
}