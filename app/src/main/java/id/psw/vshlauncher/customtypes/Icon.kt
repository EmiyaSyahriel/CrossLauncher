package id.psw.vshlauncher.customtypes

import android.graphics.Bitmap
import androidx.core.graphics.scale

data class Icon (val selected : Bitmap, val unselected : Bitmap) {
    companion object
    {
        var unselectedScale = 0.75f
        var density = 1.0f
        var size = 75

        fun fromBitmap(source: Bitmap) : Icon {
            val dim = (size*density).toInt()
            val dimUnsel = (dim * unselectedScale).toInt()
            return Icon(source.scale(dim, dim), source.scale(dimUnsel, dimUnsel))
        }
    }
}