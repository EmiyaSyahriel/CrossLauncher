package id.psw.vshlauncher

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import java.io.File

// TODO: Integrate the content info to [VshView]
class VshY {
    class VshOptions {
        var name : String = ""
        var onClick : Runnable = EmptyRunnable
    }


    companion object{
        val EmptyRunnable = Runnable { /**Do Nothing**/ }
        val TransparentBitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
        val TransparentDrawable = BitmapDrawable(TransparentBitmap)
    }

    var itemID : Int = 0
    var selectedIcon : Bitmap = TransparentBitmap
    var unselectedIcon : Bitmap = TransparentBitmap
    var text : String = ""
    var subtext : String = ""
    var onClick : Runnable = EmptyRunnable
    var options : ArrayList<VshOptions> = arrayListOf()
    var contentInfo: ContentInfo = ContentInfo()

    constructor()

    constructor(
        itemID:Int,
        name:String,
        description : String,
        icon:Drawable? = null,
        density : Float = 1f,
        onClick : Runnable? = null,
        options : ArrayList<VshOptions> = arrayListOf(),
        contentDir : File? = null)
    {
        this.itemID = itemID
        this.text = name
        this.subtext = description
        val iconBitmap = (icon ?: TransparentDrawable).toBitmap()
        val selectedSize = (70f * density).toInt()
        val unselectedSize = (50f * density).toInt()
        selectedIcon = Bitmap.createScaledBitmap(iconBitmap, selectedSize, selectedSize, false)
        unselectedIcon = Bitmap.createScaledBitmap(iconBitmap, unselectedSize, unselectedSize, false)
        this.onClick = onClick ?: EmptyRunnable
        this.options = options
        this.contentInfo = ContentInfo(contentDir)
    }
}
