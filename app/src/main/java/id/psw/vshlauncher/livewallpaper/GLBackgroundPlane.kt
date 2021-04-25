package id.psw.vshlauncher.livewallpaper

import android.opengl.GLES20
import id.psw.vshlauncher.livewallpaper.ogl.*

class GLBackgroundPlane : GLShape() {

    init{
        name = "layer0.mdl"
        shader = GLShaders.backgroundShader

        vertices.add(Vector3( 1f,   1f, 0f))
        vertices.add(Vector3(-1f,  -1f, 0f))
        vertices.add(Vector3( 1f,  -1f, 0f))
        vertices.add(Vector3(-1f,   1f, 0f))

        uv.add(Vector2(1f, 1f))
        uv.add(Vector2(0f, 0f))
        uv.add(Vector2(1f, 0f))
        uv.add(Vector2(0f, 1f))
        normals.add(Vector3(1f,1f,1f))
        normals.add(Vector3(1f,1f,1f))
        normals.add(Vector3(1f,1f,1f))
        normals.add(Vector3(1f,1f,1f))

        tris.add(3)
        tris.add(1)
        tris.add(0)
        tris.add(1)
        tris.add(2)
        tris.add(0)

        autoCalcNormal = true
    }

    override fun render() {
        shader.use()
        shader.setUniform("_ColorA", .5f, .8f, 1f)
        shader.setUniform("_ColorB", 0f, .3f, .5f)
        genBuffer()
        super.render()
    }
}