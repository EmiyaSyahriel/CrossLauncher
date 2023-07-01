@file:Suppress("UNUSED_PARAMETER")

package id.psw.vshlauncher

import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.content.res.ResourcesCompat
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.types.Ref
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.views.XmbView
import java.io.File
import java.util.*
import kotlin.experimental.and
import kotlin.reflect.KClass

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
        if(this is VSH) return this.xmbView?.context?.xmb!!
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
fun File?.combine(vararg paths : String?) : File? {
    var rv = this
    paths.forEach {
        if(rv != null && it != null)  rv = File(rv, it)
        else Logger.v("Combiner", "- Invalid Combination File : $rv '/' $it ")
    }
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

fun <TType, TReturn> TType.callOnCount(countRef: Ref<Int>, lastStateRef: Ref<TReturn>, pollEveryNCall:Int, func: (TType) -> TReturn) : TReturn {
    if(countRef.p >= pollEveryNCall){
        countRef.p = 0
        lastStateRef.p = func(this)
    }
    countRef.p++
    return lastStateRef.p
}

/** Delay ```File.exists()``` call until the call time count equal or more than ```pollEveryNCall```
 * The call result then will be cached, then the cache will be returned, Done like this since
 * File.exists() is an expensive call and calling it on every frame is basically killing the phone or
 * storage media faster
 */
fun File.delayedExistenceCheck(iTrack: Ref<Int>, lastState:Ref<Boolean>, pollEveryNCall:Int = 61) : Boolean =
    callOnCount(iTrack, lastState, pollEveryNCall) { it.exists() }

fun <K,V> MutableMap<K, V>.getOrMake(k:K, v:() -> V) : V{
    return if(containsKey(k)){
        get(k)!!
    }else{
        val rv = v()
        put(k, rv)
        rv
    }
}

fun createSerializedResolution(size: Point) : Int = (((size.x and 0xFFFF) shl 16) or (size.y and 0xFFFF))
fun readSerializedResolution(srlSize:Int, buffer: Point) = buffer.set((srlSize shr 16) and 0xFFFF, srlSize and 0xFFFF)
fun createSerializedLocale(locale: Locale?) : String = if(locale == null) "" else "${locale.language}|${locale.country}|${locale.variant}"
fun readSerializedLocale(srlLocale:String) : Locale {
    val locData = srlLocale.split('|')
    return when(locData.size){
        0 -> Locale.getDefault()
        1 -> Locale(locData[0])
        2 -> Locale(locData[0], locData[1])
        3 -> Locale(locData[0], locData[1], locData[2])
        else -> Locale.getDefault()
    }
}

@ChecksSdkIntAtLeast
fun sdkAtLeast(num : Int) : Boolean = Build.VERSION.SDK_INT >= num

private val youAccentBrightness = arrayListOf(
    0, 10, 50, 100, 200, 300, 400, 500, 600, 700, 800, 1000
)

private fun <T> getResourcesIdentifier(e:String, c:Class<T>) : Int{
    try{
        val idField = c.getDeclaredField(e)
        return idField.getInt(idField)
    }catch(e:Exception){

    }
    return 0
}

fun getMaterialYouColor(ctx:Context, accent:Int, brightness:Int, retval : Ref<Int>) : Boolean{
    if(!sdkAtLeast(Build.VERSION_CODES.S)){
        return false
    }

    val y = youAccentBrightness[brightness.coerceIn(0, youAccentBrightness.size - 1)]
    val i = getResourcesIdentifier("system_accent${accent + 1}_${y}",  android.R.color::class.java)
    if(i == 0) {
        return false
    }

    retval.p =
        if(sdkAtLeast(23)) ctx.resources.getColor(i, null)
        else ctx.resources.getColor(i)
    return true
}

inline fun <reified  E : Enum<E>> enumFromInt(i : Int) : E{
    val eis = enumValues<E>()
    for(e in eis){
        if(e.ordinal == i) return e
    }
    return eis[0]
}

fun <E : Enum<E>> Enum<E>.toInt(): Int = this.ordinal