package id.psw.vshlauncher.icontypes

import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.VshY


class AppIcon(private var context: VSH, itemID: Int, private var resolveInfo: ResolveInfo) :
    VshY(itemID) {

    private var cachedSelectedIcon : Bitmap = transparentBitmap
    private var cachedUnselectedIcon : Bitmap = transparentBitmap
    private var appLabel = resolveInfo.loadLabel(context.packageManager).toString()
    private var launchApp = Runnable {
        context.startApp(resolveInfo.activityInfo.packageName)
    }

    companion object{
        private const val selectedIconSize = 70f
        private const val unselectedIconSize = 50f
        var dynamicUnload = false
    }

    init{
        if(!dynamicUnload){
            loadIcon()
        }
    }

    override val hasOptions: Boolean = true
    override val options: ArrayList<VshOption>
        get() = arrayListOf()

    override val selectedIcon: Bitmap get() = cachedSelectedIcon
    override val unselectedIcon: Bitmap get() = cachedUnselectedIcon

    private fun loadIcon(){
        val selectedSize = (selectedIconSize * context.vsh.density).toInt()
        val unselectedSize = (unselectedIconSize * context.vsh.density).toInt()
        val loadedIcon = resolveInfo.loadIcon(context.packageManager).toBitmap()
        cachedSelectedIcon = Bitmap.createScaledBitmap(loadedIcon, selectedSize, selectedSize, false)
        cachedUnselectedIcon = Bitmap.createScaledBitmap(loadedIcon, unselectedSize, unselectedSize, false)
    }

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() = resolveInfo.activityInfo.packageName ?: "No package name"

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
}