package id.psw.vshlauncher.types

import android.graphics.Bitmap

data class XmbNotification(
    val handle:Long,
    var icon: Bitmap?,
    var title:String,
    var desc:String,
    var remainingTime : Float = 3.0f,
    val destroy : Boolean = false
)