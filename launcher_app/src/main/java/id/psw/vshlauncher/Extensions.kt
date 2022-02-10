package id.psw.vshlauncher

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import androidx.core.content.res.ResourcesCompat
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.XmbView
import java.io.File
import kotlin.experimental.and

/**
 * Cast get instance of VSH, either from the application context or the calling context
 */
val Context.vsh : VSH
    get() {
            // Check if current context is an VSH App Context
        if(this is VSH) return this
            // Check if current context's application is an VSH App Context
        if(this.applicationContext is VSH) return this.applicationContext as VSH
        return this as VSH
    }

val Context.xmb : XMB
    get() {
        // Check if current context is an XMB Activity Context
        if(this is XMB) return this
        return this as XMB
    }

fun <T> Boolean.select(a:T, b:T) : T =  if(this) a else b

val Iterable<XMBItem>.visibleItems get() = synchronized(this) { this.filter { !it.isHidden } }

infix fun Int.hasFlag(b:Int) : Boolean = this and b == b
infix fun Byte.hasFlag(b:Byte) : Boolean = this and b == b
infix fun UByte.hasFlag(b:UByte) : Boolean = this and b == b

fun Float.toLerp(a:Float, b:Float) : Float = a + ((b - a) * this)
fun Float.lerpFactor(a:Float, b:Float) : Float = (this - a) / (b - a)
fun Double.toLerp(a:Double, b:Double) : Double = a + ((b - a) * this)
fun Double.lerpFactor(a:Double, b:Double) : Double = (this - a) / (b - a)

var swapLayoutType = false

val Drawable?.hasSize : Boolean get() = if(this != null) this.intrinsicWidth > 0 && this.intrinsicHeight > 0 else false
fun File.combine(vararg paths : String) : File {
    var rv = this
    paths.forEach { rv = File(rv, it) }
    return rv
}

fun fit(sx:Float,dx:Float,sy:Float,dy:Float,w:Float,h:Float) : Float {
    return (w / sx).coerceAtMost(h / sy)
}
fun fill(sx:Float,dx:Float,sy:Float,dy:Float,w:Float,h:Float) : Float {
    return (w / sx).coerceAtLeast(h / sy)
}

fun fitFillSelect(sx:Float,dx:Float,sy:Float,dy:Float,w:Float,h:Float,select:Float) : Float {
    return when {
        select < 0.0f -> (select+1).coerceIn(0.0f, 1.0f).toLerp(0.0f, fit(sx,dx,sy,dy,w,h))
        select in 0.0f..1.0f -> select.toLerp(fit(sx,dx,sy,dy,w,h), fill(sx,dx,sy,dy,w,h))
        else -> fill(sx,dx,sy,dy,w,h) * select
    }
}
const val fPI = Math.PI.toFloat()
const val f2PI = 2.0f * fPI

val Float.nrm2Rad : Float get() = this * f2PI
val Float.nrm2Deg : Float get() = this * 360.0f
val Float.deg2Rad : Float get() = (this / 180.0f) * fPI
val Float.deg2nrm : Float get() = this / 360.0f
val Float.rad2Deg : Float get() = (this / fPI) * 180.0f
val Float.rad2Nrm : Float get() = this / f2PI

fun XmbView.getDrawable(id:Int) : Drawable?{
    return ResourcesCompat.getDrawable(context.resources, id, context.theme)
}


fun Parcel.writeByteBoolean(boolean: Boolean) = this.writeByte(boolean.select(1,0))
fun Parcel.readByteBoolean() : Boolean = this.readByte() != 0.toByte()
