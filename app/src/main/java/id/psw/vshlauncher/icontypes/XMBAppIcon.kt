package id.psw.vshlauncher.icontypes

import android.content.Context
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.VSH
import java.lang.Exception

open class XMBAppIcon (id:String, private val resolve:ResolveInfo, protected val app: Context) : XMBIcon (id){

    private var cachedActiveIcon : Bitmap? = null
    private var cachedInactiveIcon : Bitmap? = null
    private var pkgName = "Dummy"

    init{
        try{
            cachedActiveIcon = resolve.loadIcon(app.packageManager).toBitmap(75,75, Bitmap.Config.ARGB_8888)
            cachedInactiveIcon = resolve.loadIcon(app.packageManager).toBitmap(60,60, Bitmap.Config.ARGB_8888)
            pkgName = resolve.resolvePackageName
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

    override val description: String
        get() = id

    override fun onLaunch() {
        Log.d("XMBAppIcon","onLaunch : ICON $id")
        if(app is VSH){
            val vsh = app as VSH
            vsh.startApp(id)
        }
    }

    override val hasDescription: Boolean
        get() = true

    override var activeIcon: Bitmap
        get() = cachedActiveIcon ?: blankBmp
        set(value) {}

    override var inactiveIcon: Bitmap
        get() = cachedInactiveIcon ?: blankBmp
        set(value) {}

    override val hasContent: Boolean = false
}