package id.psw.vshlauncher.submodules.settings

import android.os.Build
import android.provider.Settings
import id.psw.vshlauncher.BuildConfig
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.activities.Xmb
import id.psw.vshlauncher.addAllV
import id.psw.vshlauncher.setActiveLocale
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.supportedLocaleList
import id.psw.vshlauncher.types.items.XmbAndroidSettingShortcutItem
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem
import id.psw.vshlauncher.views.dialogviews.BitManDlgView
import id.psw.vshlauncher.views.dialogviews.CustomResourceListDialogView
import id.psw.vshlauncher.views.dialogviews.TestDialogView
import id.psw.vshlauncher.views.dialogviews.UITestDialogView

class DebugSettings(private val vsh: Vsh): ISettingsCategories(vsh) {
    private fun mkItemTestDialog(): XmbSettingsItem {
        return XmbSettingsItem(vsh, "dbg_launch_dialog_test",
            R.string.dbg_launch_dialog_test, R.string.empty_string, R.drawable.category_debug, {""}){
            if(vsh.haveXmbView) vsh.safeXmbView.showDialog(TestDialogView(vsh.safeXmbView))
        }
    }

    private fun mkItemTestUIDialog(): XmbSettingsItem {
        return XmbSettingsItem(vsh, "dbg_launch_dialog_test_ui",
            R.string.dbg_launch_dialog_ui_test, R.string.empty_string, R.drawable.category_debug, {""}){
            if(vsh.haveXmbView) vsh.safeXmbView.showDialog(UITestDialogView(vsh.safeXmbView))
        }
    }

    private fun mkItemCustomFileList() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "dbg_custom_file_list",
            R.string.dbg_custom_file_list,
            R.string.empty_string,
            R.drawable.ic_short_line, { "" }
        ){
            if(vsh.haveXmbView) vsh.safeXmbView.showDialog(CustomResourceListDialogView(vsh.safeXmbView))
        }
    }

    private fun mkItemDebugThrows() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "dbg_throw_exception",
            R.string.dbg_custom_throw_unhandled_exception,
            R.string.empty_string,
            R.drawable.ic_error, { "" }
        ){
            throw Exception("This is an unhandled exception, present in log file")
        }
    }

    private fun mkItemOpenBitmapManager() : XmbSettingsItem {
        return XmbSettingsItem(
            vsh,
            "dbg_throw_window",
            R.string.dbg_bitman_info, R.string.empty_string,
            R.drawable.settings_category_display, {""})
        {
            if(vsh.haveXmbView) vsh.safeXmbView.showDialog(BitManDlgView(vsh.safeXmbView))
        }

    }

    private fun getCurrentLocaleName() : String {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            vsh.resources.configuration.locales[0].displayName
        }else{
            vsh.resources.configuration.locale.displayName
        }
    }

    private fun mkItemChangeLocale(): XmbSettingsItem {

        val click : () -> Unit = {
            vsh.xmbView?.showSideMenu(true)
        }
        val menu = { m : XmbSettingsItem ->
            m.hasMenu = true
            val dMenuItems = arrayListOf<XmbMenuItem>()
            vsh.supportedLocaleList.forEachIndexed { i, it ->
                val item = XmbMenuItem.XmbMenuItemLambda(
                    { it?.displayName ?: "System Default" },
                    { false }, i)
                {
                    vsh.setActiveLocale(it)
                }
                dMenuItems.add(item)
            }
            m.menuItems = dMenuItems
        }
        return XmbSettingsItem(vsh, "settings_system_language",
            R.string.settings_system_language, R.string.settings_system_language_description,
            R.drawable.icon_language, ::getCurrentLocaleName, click)
            .apply(menu)
    }

    override fun createCategory(): XmbSettingsCategory {
        return XmbSettingsCategory(vsh,
            SettingsSubmodule.CATEGORY_SETTINGS_DEBUG,
            R.drawable.category_debug,
            R.string.settings_category_debug_name,
            R.string.settings_category_debug_desc
        ).apply {
            isSettingHidden = { !(BuildConfig.DEBUG || vsh.showDebuggerCount >= 8) }

            content.addAllV(
                mkItemTestDialog(),
                mkItemTestUIDialog(),
                mkItemCustomFileList()
            )

            if(BuildConfig.DEBUG){
                content.addAllV(
                    mkItemDebugThrows(),
                    mkItemOpenBitmapManager(),
                    mkItemOpenFakeSettings(),
                    mkItemOpenSelfAsSettings()
                )
            }

            content.add(mkItemChangeLocale())

        }
    }

    private fun mkItemOpenFakeSettings(): XmbAndroidSettingShortcutItem {
        return XmbAndroidSettingShortcutItem(
            vsh, R.drawable.category_setting,
            R.string.android_dbg_setting_invalid_page_name,
            R.string.android_dbg_setting_invalid_page_desc,
            "id.psw.vshlauncher.FakeSettings\$DoNotCreateThisClassPlz"
        )
    }

    private fun mkItemOpenSelfAsSettings(): XmbAndroidSettingShortcutItem {
        return XmbAndroidSettingShortcutItem(
            vsh, R.drawable.category_setting,
            R.string.android_dbg_setting_self_name,
            R.string.android_dbg_setting_self_desc,
            Xmb.Companion::class.java.canonicalName ?: "id.psw.vshlauncher.activities.Xmb"
        )
    }
}