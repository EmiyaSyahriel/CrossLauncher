package id.psw.vshlauncher

import android.content.Intent
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBSettingsItem
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.dialogviews.TextDialogView
import id.psw.vshlauncher.views.showDialog
import kotlin.system.exitProcess

fun VSH.addHomeScreen(){
    addToCategory(VSH.ITEM_CATEGORY_HOME, XMBSettingsItem(this, "show_home_screen", R.string.app_hide_menu, R.string.app_hide_menu_desc,
        R.drawable.icon_hide_menu,{ "" }
    ){
        xmbView?.switchPage(VshViewPage.HomeScreen)
    })

    addToCategory(VSH.ITEM_CATEGORY_HOME, XMBSettingsItem(this, "home_screen_exit_app",
        R.string.home_screen_exit_name,
        R.string.home_screen_exit_desc,
        R.drawable.ic_close, { "" }
    ){
        exitProcess(0)
    }.apply {
        checkIsHidden = { !shouldShowExitOption }
    })

    addToCategory(VSH.ITEM_CATEGORY_HOME, XMBSettingsItem(this, "home_screen_restart_app",
        R.string.common_reboot,
        R.string.home_screen_reboot_desc,
        R.drawable.icon_refresh, { "" }
    ){
        xmbView?.showDialog(TextDialogView(vsh)
            .setData(null, vsh.getString(R.string.reboot_dlg_confirm_title), vsh.getString(R.string.reboot_dlg_confirm_content))
            .setNegative(vsh.getString(android.R.string.cancel)){ it.finish(VshViewPage.MainMenu) }
            .setPositive(vsh.getString(R.string.common_reboot)){ vsh.restart() })
    })
}
