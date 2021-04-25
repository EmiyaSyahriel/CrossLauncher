package id.psw.vshlauncher.customtypes

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import id.psw.vshlauncher.toBitmap

class Icon (source: Bitmap, size:Int) {
    companion object
    {
        var unselectedScale = 0.75f
        var density = 1.0f
        val blankBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
    }

    lateinit var selected : Bitmap
    lateinit var unselected : Bitmap

    init {
        reload(source, size)
    }

    fun unload(){
        if(unselected != blankBitmap) unselected.recycle()
        if(selected != blankBitmap) selected.recycle()

        selected = blankBitmap
        unselected = blankBitmap
    }

    fun reload(source:Bitmap, size:Int){
        val dim = (size*density).toInt()
        val dimUnsel = (dim * unselectedScale).toInt()
        selected = source.scale(dim, dim)
        unselected = source.scale(dimUnsel, dimUnsel)
    }
}