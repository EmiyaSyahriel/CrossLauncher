package id.psw.vshlauncher.icontypes

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import java.lang.Exception

open class XMBAppIcon (id:String, private val resolve:ResolveInfo, protected val app: Context) : XMBIcon (id){

    private var cachedActiveIcon : Bitmap? = null
    private var cachedInactiveIcon : Bitmap? = null
    private var pkgName = "Dummy"

    init{
        fillMenu()
        try{
            cachedActiveIcon = resolve.loadIcon(app.packageManager).toBitmap(75,75, Bitmap.Config.ARGB_8888)
            cachedInactiveIcon = resolve.loadIcon(app.packageManager).toBitmap(60,60, Bitmap.Config.ARGB_8888)
            pkgName = resolve.resolvePackageName
        }catch (e:Exception) {  }
    }

    private fun fillMenu() {
        menu.clear()
        val launch = XMBMenuEntry("menu_appicon_launch").apply {
            this.name = app.getString(R.string.app_launch)
            this.selectable = true
            this.onClick = Runnable { onLaunch() }
        }
        val viewps = XMBMenuEntry("menu_appicon_open_playstore").apply {
            this.name = app.getString(R.string.app_find_on_playstore)
            this.selectable = true
            this.onClick = Runnable { findOnPlayStore() }
        }
        menu.add(launch)
        menu.add(viewps)
    }

    private fun findOnPlayStore() {
        try{
            app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkgName")))
        }catch(exc:Exception){
            Log.e("VSH::AppIcon","Cannot find any app to view market:// URL Scene to Open Play Store")
        }
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

    override val hasMenu: Boolean = true

    override val hasDescription: Boolean = true

    override var activeIcon: Bitmap
        get() = cachedActiveIcon ?: blankBmp
        set(value) {}

    override var inactiveIcon: Bitmap
        get() = cachedInactiveIcon ?: blankBmp
        set(value) {}

    override val hasContent: Boolean = false
}