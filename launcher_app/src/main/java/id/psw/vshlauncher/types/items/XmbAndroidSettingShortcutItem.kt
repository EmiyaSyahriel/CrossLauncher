package id.psw.vshlauncher.types.items

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.views.dialogviews.ConfirmDialogView
import id.psw.vshlauncher.xmb

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
        ?.toBitmap(Vsh.ITEM_BUILTIN_ICON_BITMAP_SIZE,Vsh.ITEM_BUILTIN_ICON_BITMAP_SIZE)
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

        // For testing
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
            if(e is ActivityNotFoundException){
                askUserShouldOpenMainPage()
            }else{
                vsh.postNotification(null, e.javaClass.name, e.message ?: "Unknown cause", 10.0f)
            }
        }
    }

    private fun askUserShouldOpenMainPage() {
        val xv = vsh.xmbView ?: return
        xv.showDialog(
            ConfirmDialogView(xv, vsh.getString(R.string.error_setting_not_found), R.drawable.category_setting,
                vsh.getString(R.string.error_setting_not_found_description).format(displayName)){confirmed ->
                if(confirmed){
                    launchMainSettings()
                }
            }
        )
    }

    private fun launchMainSettings() {
        try{
            val i = Intent(Settings.ACTION_SETTINGS)
            i.flags = i.flags or Intent.FLAG_ACTIVITY_NEW_TASK
            vsh.startActivity(i)
        }catch(e:Exception){
            vsh.postNotification(null, e.javaClass.name, e.message ?: "Unknown cause", 10.0f)
        }
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launchSetting
}