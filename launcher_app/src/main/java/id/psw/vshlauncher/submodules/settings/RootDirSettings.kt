package id.psw.vshlauncher.submodules.settings

import android.content.Intent
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem
import id.psw.vshlauncher.views.dialogviews.SystemUpdateDialogView
import id.psw.vshlauncher.xmb

class RootDirSettings (private val vsh: Vsh) : ISettingsCategories(vsh) {

    fun settingsAddInstallPackage(): XmbItem {
        val xi = XmbSettingsItem(vsh, "settings_install_package", R.string.settings_install_package, R.string.empty_string, R.drawable.ic_folder,{
            ""
        }){
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
            i.type = "*/*"
            try{
                vsh.xmbView?.context?.xmb?.startActivityForResult(Intent.createChooser(i, vsh.getString(R.string.settings_install_package)), Vsh.ACT_REQ_INSTALL_PACKAGE)
            }catch(_:Exception){}
        }
        return xi
    }

    private fun settingsAddSystemUpdate(): XmbItem {
        val xi = XmbSettingsItem(vsh, "settings_system_update", R.string.settings_system_update_name,
            R.string.settings_system_update_desc,
            R.drawable.ic_sync_loading, { "" }
        ){
            vsh.safeXmbView.showDialog(SystemUpdateDialogView(vsh.safeXmbView))
        }
        return xi
    }

    fun createCategories() : Array<XmbItem> {
        return arrayOf(settingsAddInstallPackage(), settingsAddSystemUpdate())
    }

    @Deprecated("For this class use [createCategories] instead")
    override fun createCategory(): XmbSettingsCategory { throw Exception("Use Create Categories instead") }
}