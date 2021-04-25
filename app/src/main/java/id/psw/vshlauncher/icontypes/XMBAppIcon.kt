package id.psw.vshlauncher.icontypes

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import java.lang.Exception

open class XMBAppIcon (id:String, private val resolve:ResolveInfo, protected val app: Context) : XMBIcon (id){

    private var cachedActiveIcon : Bitmap? = null
    private var cachedInactiveIcon : Bitmap? = null

    init{
        try{
            cachedActiveIcon = resolve.loadIcon(app.packageManager).toBitmap(75,75, Bitmap.Config.ARGB_8888)
            cachedInactiveIcon = resolve.loadIcon(app.packageManager).toBitmap(60,60, Bitmap.Config.ARGB_8888)
        }catch (e:Exception) {  }
    }

    override val name: String
        get(){
            var retval = "Unknown App"
            try{
                retval = resolve.loadLabel(app.packageManager).toString()
            }catch(e:Exception){}
            return retval
        }

    override var activeIcon: Bitmap
        get() = cachedActiveIcon ?: blankBmp
        set(value) {}

    override var inactiveIcon: Bitmap
        get() = cachedInactiveIcon ?: blankBmp
        set(value) {}

    override val hasContent: Boolean = false
}