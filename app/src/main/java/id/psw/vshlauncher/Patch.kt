package id.psw.vshlauncher

import android.graphics.Canvas
import android.graphics.Color
import java.lang.StringBuilder
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