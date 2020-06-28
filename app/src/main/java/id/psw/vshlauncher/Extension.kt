package id.psw.vshlauncher

import android.graphics.PointF
import androidx.core.graphics.minus
import java.lang.Math.round

fun PointF.distanceTo(other : PointF) : Float{
    return PointF(other.x - this.x, other.y - this.y).length()
}

fun Long.toSize() : String{
    var size = this.toDouble()
    var sizeIndex = 0
    while(size > 1024){
        size /= 1024
        sizeIndex++
    }
    size = round(size * 100.0) / 100.0
    val sizeName = MimeTypeDict.sizeMap.getOrNull(sizeIndex) ?: "B"
    return "$size$sizeName"
}

fun <T> Boolean.choose(ifTrue : T, ifFalse: T):T{
    return if (this) {ifTrue} else {ifFalse}
}