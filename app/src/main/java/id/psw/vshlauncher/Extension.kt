package id.psw.vshlauncher

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.graphics.minus
import androidx.core.view.ViewCompat
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



fun View.getSystemPadding() : Rect{
    val res = context.resources
    val retval = Rect(0,0,res.displayMetrics.widthPixels, res.displayMetrics.heightPixels)
    val navBarId = res.getIdentifier("navigation_bar_height","dimen","android")
    val statusBarId = res.getIdentifier("status_bar_height", "dimen", "android")
    if(res.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
        retval.left = res.getDimensionPixelSize(navBarId)
        retval.right = res.displayMetrics.widthPixels - res.getDimensionPixelSize(navBarId)
        retval.top = res.getDimensionPixelSize(statusBarId)
    }else{
        retval.bottom = res.displayMetrics.heightPixels - res.getDimensionPixelSize(navBarId)
        retval.top = res.getDimensionPixelSize(statusBarId)
    }

    Log.d("extSystemPadding", "Renderable Area : $retval")
    return retval
}

