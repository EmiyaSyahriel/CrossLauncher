package id.psw.vshlauncher

import java.lang.StringBuilder
import kotlin.math.floor

fun Float.toLerp(a:Float, b:Float) : Float{
    return a + ((b-a) * this);
}

fun Float.floorToInt() : Int{
    return floor(this).toInt()
}