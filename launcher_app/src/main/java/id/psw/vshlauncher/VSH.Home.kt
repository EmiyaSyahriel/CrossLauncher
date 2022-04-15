package id.psw.vshlauncher

import android.content.Intent
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBSettingsItem
import id.psw.vshlauncher.views.VshViewPage
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
}
