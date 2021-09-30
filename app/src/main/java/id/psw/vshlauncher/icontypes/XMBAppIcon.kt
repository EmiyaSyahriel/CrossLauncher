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
import id.psw.vshlauncher.activities.AppInfoActivity
import java.lang.Exception

open class XMBAppIcon (id:String, private val resolve:ResolveInfo, protected val app: Context) : XMBIcon (id){

    private var cachedActiveIcon : Bitmap? = null
    private var cachedInactiveIcon : Bitmap? = null
    private var pkgName = "Dummy"

    companion object{
        const val TAG = "VSH::AppIcon"

    }

    private enum class DataType {
        Data,
        Obb,
        Customization
    }

    init{
        fillMenu()
        try{

            cachedActiveIcon = resolve.loadIcon(app.packageManager).toBitmap(75,75, Bitmap.Config.ARGB_8888)
            cachedInactiveIcon = resolve.loadIcon(app.packageManager).toBitmap(60,60, Bitmap.Config.ARGB_8888)
            pkgName = resolve.activityInfo.packageName
        }catch (e:Exception) {
            Log.e(TAG, "App Data Initialization Error", e)
        }
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
        val openinfo = XMBMenuEntry("menu_appicon_details").apply {
            name = app.getString(R.string.app_show_details)
            this.selectable = true
            this.onClick = Runnable { showAppDetails() }
        }
        val uninstall = XMBMenuEntry("menu_appicon_uninstall").apply {
            name = app.getString(R.string.app_uninstall)
            this.selectable = true
            this.onClick = Runnable { requestAppUninstall() }
        }
        menu.add(launch)
        menu.add(viewps)
        menu.add(openinfo)
        menu.add(uninstall)
    }

    private fun requestAppUninstall() {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$pkgName")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        app.startActivity(intent)
    }

    private fun showAppDetails() {
        try{
            val dispint = Intent(app, AppInfoActivity::class.java)
            dispint.putExtra(AppInfoActivity.ARG_INFO_PKG_NAME_KEY, pkgName)
            app.startActivity(dispint)
        }catch (exc:Exception){
            Log.e(TAG,"Error displaying app info")
        }
    }

    private fun findOnPlayStore() {
        try{
            app.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkgName")))
        }catch(exc:Exception){
            Log.e(TAG,"Cannot find any app to view market:// URL Scene to Open Play Store")
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
        Log.d(TAG,"onLaunch : ICON $id")
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