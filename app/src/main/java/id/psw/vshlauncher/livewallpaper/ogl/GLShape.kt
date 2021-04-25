package id.psw.vshlauncher.livewallpaper.ogl

import android.opengl.GLES20
import android.opengl.GLU
import android.opengl.GLUtils
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.log

open class GLShape {

    var name = "GLShape"
    var position = Vector3.zero
    var scale = Vector3.one

    val vertices = arrayListOf<Vector3>()
    val normals = arrayListOf<Vector3>()
    val uv = arrayListOf<Vector2>()
    val tris = arrayListOf<Int>()
    var shader : GLShader = GLShaders.blankShader
    var vbo = 0
    var autoCalcNormal = false
    open val renderUsage = GLES20.GL_STATIC_DRAW
    lateinit var vertBuffer : FloatBuffer

    init{
        val ii = IntBuffer.allocate(1)
        GLES20.glGenBuffers(1, ii)
        vbo = ii[0]
    }

    companion object {

        // Adapted from Source : https://stackoverflow.com/a/1815288
        private fun createNormal(v : Array<Vector3>): Vector3{
            val v1 = Vector3.zero
            val v2 = Vector3.zero
            val retval = Vector3.zero

            // 1 to 0
            v1.x = v[0].x - v[1].x
            v1.y = v[0].y - v[1].y
            v1.y = v[0].y - v[1].y

            // 2 to 1
            v2.x = v[1].x - v[2].x
            v2.y = v[1].y - v[2].y
            v2.z = v[1].z - v[2].z

            // Cross product
            retval.x = (v1.y * v2.z) - (v1.z * v2.y)
            retval.y = (v1.z * v2.x) - (v1.x * v2.z)
            retval.z = (v1.x * v2.y) - (v1.y * v2.x)
            retval.normalize()

            return retval
        }
        const val TAG = "gcm.gl#GLShape"
    }

    open fun genBuffer() {
        Log.d(TAG, "GLShape[$name] : Rebuilding Vertex Buffer #$vbo")
        calculateNormal()
        val bb = ByteBuffer.allocateDirect(tris.size * 8 * 4).run {
            this.order(ByteOrder.nativeOrder())
        }
        vertBuffer = bb.asFloatBuffer()
        tris.forEach {
            vertices[it].appendTo(vertBuffer, position, scale)
            normals[it].appendTo(vertBuffer, Vector3.zero, Vector3.one)
            uv[it].appendTo(vertBuffer, Vector2.zero, Vector2.one)
        }
        vertBuffer.compact()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertBuffer.capacity() * 4,
            vertBuffer,
            renderUsage
        )
        Log.d(TAG, "GLShape[$name] : Vertex Buffer Rebuilt - Tris : ${(tris.size/3)} | Verts : ${vertices.size}}")
    }

    /// Format :
    /// vertex  normal  u v
    /// x y z   x y z   x y
    /// 0 0 0   0 0 0   0 0

    open fun calculateNormal()
    {
        if(autoCalcNormal){

            Log.d(TAG, "GLShape[$name] : Recalculating normal...")
            var offset = 0
            while (offset < tris.size){
                val t0 = tris[offset+0]
                val t1 = tris[offset+1]
                val t2 = tris[offset+2]
                val norm = createNormal(arrayOf(vertices[t0], vertices[t1], vertices[t2]))
                normals[t0] += norm
                normals[t1] += norm
                normals[t2] += norm
                offset += 3;
            }
            offset = 0
            normals.forEach { it.normalize() }
            Log.d(TAG, "GLShape[$name] : Recalculating normal...")
        }
    }

    open fun render(){
        shader.use()
        shader.enableAttrib("vpos")
        shader.enableAttrib("normal")
        shader.enableAttrib("uv")

        val vpos = shader.attribLoc("vpos")
        val normal = shader.attribLoc("normal")
        val uv = shader.attribLoc("uv")

        GLES20.glVertexAttribPointer(vpos, 3, GLES20.GL_FLOAT, false, 8*4, 0)
        GLES20.glVertexAttribPointer(normal, 3, GLES20.GL_FLOAT, false, 8*4, 3 *4 )
        GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 8*4, 6 * 4)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertBuffer.capacity())
        shader.disableAttrib("vpos")
        shader.disableAttrib("normal")
        shader.disableAttrib("uv")
    }
}