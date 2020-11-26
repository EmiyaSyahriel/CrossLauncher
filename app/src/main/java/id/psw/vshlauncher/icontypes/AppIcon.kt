package id.psw.vshlauncher.icontypes

import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY
import id.psw.vshlauncher.toSize
import java.io.File
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AppIcon(private var context: VSH, itemID: Int, private var resolveInfo: ResolveInfo) :
    VshY(itemID) {

    private var cachedSelectedIcon : Bitmap = transparentBitmap
    private var cachedUnselectedIcon : Bitmap = transparentBitmap

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

    private var launchApp = Runnable {
        context.startApp(resolveInfo.activityInfo.packageName)
    }

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
        private const val selectedIconSize = 70f
        private const val unselectedIconSize = 50f
        var dynamicUnload = false
        var descriptionText : DescriptionText = DescriptionText.PackageName
    }

    init{
        if(!dynamicUnload){
            loadIcon()
        }
    }


    private val appOptions =
        VshOptionsBuilder()
            .add("Launch", launchApp)
            .add("Uninstall") { uninstallApp() }
            .add("Find on Play Store") { openOnPlayStore() }
            .build()

    override val hasOptions: Boolean = true
    override val options: ArrayList<VshOption>
        get() = appOptions

    override val selectedIcon: Bitmap get() = cachedSelectedIcon
    override val unselectedIcon: Bitmap get() = cachedUnselectedIcon

    private fun loadIcon(){
        val selectedSize = (selectedIconSize * context.vsh.density).toInt()
        val unselectedSize = (unselectedIconSize * context.vsh.density).toInt()
        val loadedIcon = resolveInfo.loadIcon(context.packageManager).toBitmap()
        cachedSelectedIcon = Bitmap.createScaledBitmap(loadedIcon, selectedSize, selectedSize, false)
        cachedUnselectedIcon = Bitmap.createScaledBitmap(loadedIcon, unselectedSize, unselectedSize, false)
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
        if (cachedSelectedIcon != transparentBitmap) cachedSelectedIcon.recycle()
        if (cachedUnselectedIcon != transparentBitmap) cachedUnselectedIcon.recycle()

        cachedSelectedIcon = transparentBitmap
        cachedUnselectedIcon = transparentBitmap
    }

    override fun onHidden() {
        if(dynamicUnload) unloadIcon()
    }

    override fun onScreen() {
        if(dynamicUnload) loadIcon()
    }

    override val name: String
        get() = appLabel

    override val onLaunch: Runnable
        get() = launchApp

    private fun openOnPlayStore(){
        try{
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse("http://play.google.com/store/apps/details?id=${packageName}")
            context.startActivity(i)
        }catch (e:Exception){
            Toast.makeText(context, "Cannot open play store for the specified application", Toast.LENGTH_LONG).show()
        }
    }
}