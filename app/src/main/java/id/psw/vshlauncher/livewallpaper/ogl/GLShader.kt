package id.psw.vshlauncher.livewallpaper.ogl

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.InputStream
import java.nio.charset.Charset

class GLShader{

    companion object{
        const val TAG = "gcm.gl"
    }

    var progid:Int = 0
    var vertid:Int = 0
    var fragid:Int = 0
    var shaderName = "_NO_SHADER_"
    var vertSrc = ""
    var fragSrc = ""

    constructor(context: Context, fileName:String) {
        shaderName = fileName
        vertSrc = context.assets.open("$fileName.vert").readString(Charsets.UTF_8)
        fragSrc = context.assets.open("$fileName.frag").readString(Charsets.UTF_8)
        init()
    }

    constructor(fileName:String, vertSrc:String, fragSrc:String){
        shaderName = fileName
        this.vertSrc = vertSrc
        this.fragSrc = fragSrc
        init()
    }

    fun init(){
        progid = GLES20.glCreateProgram()
        vertid = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        fragid = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)

        GLES20.glShaderSource(vertid, vertSrc)
        GLES20.glShaderSource(fragid, fragSrc)
        GLES20.glCompileShader(vertid)
        GLES20.glCompileShader(fragid)

        GLES20.glAttachShader(progid, vertid)
        GLES20.glAttachShader(progid, fragid)
        GLES20.glLinkProgram(progid)

        val vertInfo = GLES20.glGetShaderInfoLog(vertid)
        val fragInfo = GLES20.glGetShaderInfoLog(fragid)
        val progInfo = GLES20.glGetProgramInfoLog(progid)

        if(vertInfo.length + fragInfo.length + progInfo.length > 0){
            Log.d(TAG, "Shader \"$shaderName\" Compilation Info : \n Vertex Shader : $vertInfo \n Fragment Shader : $fragInfo \n Program : $progInfo")
        }else{
            Log.d(TAG, "Shader program \"$shaderName\" successfully compiled : ${progid.toString(16)} (${vertid.toString(16)}, ${fragid.toString(16)})");
        }
    }

    fun use(){
        GLES20.glUseProgram(progid)
    }

    private fun uniformLoc(uniform: String) : Int{
        val retval =  GLES20.glGetUniformLocation(progid, uniform)
        // Log.d(TAG, "Uniform $uniform of $shaderName : 0x${retval.toString(16)}")
        return retval
    }

    fun attribLoc(attr:String):Int{
        val retval = GLES20.glGetAttribLocation(progid, attr)
        // Log.d(TAG, "Attribute $attr of $shaderName : 0x${retval.toString(16)}")
        return retval
    }

    fun setUniform(uniform:String, x:Float){ GLES20.glUniform1f(uniformLoc(uniform), x) }
    fun setUniform(uniform:String, x:Float, y:Float){ GLES20.glUniform2f(uniformLoc(uniform), x,y) }
    fun setUniform(uniform:String, x:Float, y:Float, z:Float){ GLES20.glUniform3f(uniformLoc(uniform), x,y,z) }
    fun setUniform(uniform:String, x:Float, y:Float, z:Float, w:Float){ GLES20.glUniform4f(uniformLoc(uniform), x,y,z,w) }

    fun enableAttrib(attrib:String){ GLES20.glEnableVertexAttribArray(attribLoc(attrib)) }
    fun disableAttrib(attrib:String){ GLES20.glDisableVertexAttribArray(attribLoc(attrib)) }


    fun InputStream.readString(charset: Charset) : String
    {
        val sb = StringBuilder()
        val array = this.bufferedReader(charset)
        array.forEachLine { sb.appendLine(it) }
        return sb.toString()
    }

}
