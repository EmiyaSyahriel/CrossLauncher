package id.psw.vshlauncher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

class VshX {

    companion object{
        val TransparentBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
    }

    var id : String= ""
    var itemY = 0
    var unselectedIcon : Bitmap = TransparentBitmap
    var selectedIcon : Bitmap = TransparentBitmap
    var name : String = ""
    var items : ArrayList<VshY> = arrayListOf()

    constructor()

    constructor(id:String, name:String, icon: Drawable?, density:Float){
        var iconBitmap = TransparentBitmap
        val defSize = (density * 75f).toInt()
        if(icon is BitmapDrawable){
            iconBitmap = icon.bitmap
        }else{
            if(icon != null){
                iconBitmap = Bitmap.createBitmap(defSize, defSize, Bitmap.Config.ARGB_8888)
                val renderCanvas = Canvas(iconBitmap)
                icon.setBounds(0,0,defSize, defSize)
                icon.draw(renderCanvas)
            }
        }
        selectedIcon = Bitmap.createScaledBitmap(iconBitmap, (density * 75).toInt(), (density * 75).toInt(), false)
        unselectedIcon = Bitmap.createScaledBitmap(iconBitmap, (density * 50).toInt(), (density * 50).toInt(), false)
        this.name = name
        this.id = id
    }

    fun getItemBy(id:Int): VshY? {
        return items.find { it.itemID == id }
    }
}