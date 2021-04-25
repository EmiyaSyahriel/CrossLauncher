package id.psw.vshlauncher.icontypes

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.*
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.views.VshView
import java.io.File
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AppIcon(var context: VSH, itemID: String, private var resolveInfo: ResolveInfo) :
    XMBIcon(itemID) {

    /// region Mostly for Cosmetics
    private var apkFile = File(resolveInfo.activityInfo.applicationInfo.sourceDir)
    private var pkgInfo = context.packageManager.getPackageInfo(resolveInfo.activityInfo.packageName, 0)

    private var packageName = resolveInfo.activityInfo.processName
    private var activityPackageName = pkgInfo.packageName
    private var appSizeByte = apkFile.length()
    private var appSize = appSizeByte.toSize()
    private var updateDate = SimpleDateFormat.getDateTimeInstance().format(Date(apkFile.lastModified()))

    @Suppress("DEPRECATION") // We need to support down to Android 4.4, where longVersionCode can't be resolved
    private var versionCode = pkgInfo.versionCode.toString()
    private var versionName = pkgInfo.versionName

    /// endregion

    private var appLabel = resolveInfo.loadLabel(context.packageManager).toString()

    enum class DescriptionText {
        PackageName,
        ActivityPackageName,
        AppSize,
        UpdateDate,
        VersionCode,
        VersionName,
        VersionCodeAndName
    }

    companion object{
        const val selectedIconSize = 70f
        const val unselectedIconSize = 50f
        var dynamicUnload = false
        var descriptionText : DescriptionText = DescriptionText.PackageName
    }

    init{
        if(!dynamicUnload){
            loadIcon()
        }
    }

    private fun loadIcon(){
        val loadedIcon = resolveInfo.loadIcon(context.packageManager).toBitmap()
        //val customIcon = context.getApp
        icon.reload(loadedIcon, selectedIconSize.toInt())
    }

    private fun uninstallApp(){
        context.uninstallApp(packageName)
    }

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() {
            return when(descriptionText){
                DescriptionText.PackageName -> packageName
                DescriptionText.ActivityPackageName -> activityPackageName
                DescriptionText.AppSize -> appSize
                DescriptionText.UpdateDate -> updateDate
                DescriptionText.VersionCode -> versionCode
                DescriptionText.VersionName -> versionName
                DescriptionText.VersionCodeAndName -> "$versionName ($versionCode)"
                else -> ""
            }
        }

    private fun unloadIcon(){
        // avoid recycling default bitmaps
        icon.unload()
    }

    fun onHidden() {
        if(dynamicUnload) unloadIcon()
    }

    fun onScreen() {
        if(dynamicUnload) loadIcon()
    }

    override val name: String
        get() = appLabel

    override fun onLaunch(){
        context.startApp(resolveInfo.activityInfo.packageName)
    }

    private fun openOnPlayStore(){
        try{
            context.launchURL("http://play.google.com/store/apps/details?id=${packageName}")
        }catch (e:Exception){
            Toast.makeText(context, "Cannot open play store for the specified application", Toast.LENGTH_LONG).show()
        }
    }
}