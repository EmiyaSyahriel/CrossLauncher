package id.psw.vshlauncher.types.items

import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.types.XmbItem

class XmbAndroidSettingShortcutItem(
    val vsh: Vsh,
    @DrawableRes private val iconId:Int,
    @StringRes private val nameId:Int,
    @StringRes private val descId: Int,
    private val intentLaunchId:String
) : XmbItem(vsh) {
    override val displayName: String get() = vsh.getString(nameId)
    override val description: String get() = vsh.getString(descId)
    override val hasDescription: Boolean
        get() = description.isNotBlank()
    override val isIconLoaded: Boolean = true
    override val icon: Bitmap = ResourcesCompat
        .getDrawable(vsh.resources, iconId, null)
        ?.toBitmap(50,50)
        ?: TRANSPARENT_BITMAP
    override val hasIcon: Boolean
        get() = icon != TRANSPARENT_BITMAP

    override var hasContent: Boolean = false
    override var content: ArrayList<XmbItem> = arrayListOf()

    override val id: String = intentLaunchId
    private var _isActivityExists = false
    override val isHidden: Boolean
        get() = !_isActivityExists

    init {
        val i = Intent(intentLaunchId)
        i.flags = i.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        _isActivityExists = i.resolveActivityInfo(vsh.packageManager, 0) != null

        if(intentLaunchId.startsWith("id.psw.vshlauncher")){
            _isActivityExists = true
        }
    }

    private fun launchSetting(xmb:XmbItem){
        try{
            val i = Intent(intentLaunchId)
            i.flags = i.flags or Intent.FLAG_ACTIVITY_NEW_TASK
            vsh.startActivity(i)
        }catch(e:Exception){
            vsh.postNotification(null, e.javaClass.name, e.message ?: "Unknown cause", 10.0f)
        }
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launchSetting
}