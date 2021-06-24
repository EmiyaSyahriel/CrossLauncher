package id.psw.vshlauncher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.view.PixelCopy
import android.widget.VideoView
import java.io.File
import java.lang.Exception
import java.lang.Integer.parseInt
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

fun Float.toLerp(a:Float, b:Float) : Float{
    return a + ((b-a) * this)
}
fun Float.toLerp(a:Int, b:Int) : Int{
    return (a + ((b-a) * this)).roundToInt()
}

fun Float.floorToInt() : Int{
    return floor(this).toInt()
}

fun Float.toLerpColor(a:Int, b: Int):Int{
    val alpha = (Color.alpha(a) + ((Color.alpha(b) - Color.alpha(a)) * this)).toInt()
    val red = (Color.red(a) + ((Color.red(b) - Color.red(a)) * this)).toInt()
    val green = (Color.green(a) + ((Color.green(b) - Color.green(a)) * this)).toInt()
    val blue = (Color.blue(a) + ((Color.blue(b) - Color.blue(a)) * this)).toInt()
    return Color.argb(alpha,red,green,blue)
}

fun Float.toMultiLerpColor(array:ArrayList<Int>) : Int{
    val t = this * (array.size-1)
    val top = array[ceil(t).toInt()]
    val bottom = array[floor(t).toInt()]
    return (t % 1.0f).toLerpColor(top, bottom)
}

fun Float.pingpong(max:Float, scale:Float):Float{
    return Math.abs((((this % max) - (0.5f * max)) / max ) * 2f) * scale
}

fun Int.mp00():String{
    if( this < 0){
        return "00"
    }
    return if(this > 9) this.toString() else "0$this"
}

fun Bitmap.getDominantColorMean(alsoCalculateAlpha:Boolean = false):Int{
    var r = 0
    var g = 0
    var b = 0
    var a = if(alsoCalculateAlpha) 0 else 255
    var validPixels = 0

    val colors = intArrayOf(width * height)
    getPixels(colors, 0, 0, 0, 0, width, height)
    colors.forEach {
        val valid = if(alsoCalculateAlpha) true else Color.alpha(it) > 1
        if(valid){
            r += Color.red(it)
            g += Color.red(it)
            b += Color.red(it)
            if(alsoCalculateAlpha) a += Color.alpha(it)
            validPixels++
        }
    }

    val finalAlpha = if(alsoCalculateAlpha) a / validPixels else 255
    return Color.argb(finalAlpha,r/validPixels,g/validPixels,b/validPixels)
}

fun VideoView.getFrame(width:Int, height:Int, callback: (Bitmap?) -> Unit){
    val bitmap : Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    try{
        val ht = HandlerThread("")

    }catch(e:Exception){}
}

fun String.hexColorToInt():Int{
    if(this.length <= 8) throw IllegalArgumentException("Please use #AARRGGBB hex format, e.g #FF00FF00 for opaque green")
    if(this[0] != '#') throw IllegalArgumentException("Use # prefix please")
    val bd = StringBuilder(2)
    bd.clear().append(this[1]).append(this[2])
    val aa = parseInt(bd.toString(), 16)
    bd.clear().append(this[3]).append(this[4])
    val rr = parseInt(bd.toString(), 16)
    bd.clear().append(this[5]).append(this[6])
    val gg = parseInt(bd.toString(), 16)
    bd.clear().append(this[7]).append(this[8])
    val bb = parseInt(bd.toString(), 16)
    return Color.argb(aa,rr,gg,bb)
}

fun pathCombine(vararg files:String): String{
    val retval = StringBuilder()
    for(i in files.indices){
        val isLast = i > files.size - 1
        val combines = if(!isLast) files[i] + File.separatorChar else files[i]
        retval.append(combines)
    }
    return retval.toString()
}