package id.psw.vshlauncher

import android.os.Build
import id.psw.vshlauncher.types.items.XMBSettingsCategory
import id.psw.vshlauncher.types.items.XMBSettingsItem
import id.psw.vshlauncher.views.XMBLayoutType

object SettingsCategoryID {
    const val CATEGORY_SETTINGS_DISPLAY = "settings_category_display"
    const val CATEGORY_SETTINGS_DEBUG = "settings_category_debug"
    const val CATEGORY_SETTINGS_SYSTEMINFO = "settings_category_systeminfo"
    const val CATEGORY_SETTINGS_SYSTEM = "settings_category_system"
}

fun VSH.fillSettingsCategory(){
    threadPool.execute {
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryDisplay())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategorySystem())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryInfo())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryDebug())
    }
}

private fun VSH.createCategorySystem() : XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_SYSTEM,
        R.drawable.category_setting,
        R.string.settings_category_system_name,
        R.string.settings_category_system_desc
    ).apply {

        content.add(
            XMBSettingsItem(vsh, "settings_system_show_fps",
                R.string.settings_system_show_fps_name,
                R.string.settings_system_show_fps_desc,
                R.drawable.category_setting,
                { showLauncherFPS.select(vsh.getString(R.string.common_yes),vsh.getString(R.string.common_no))  }
            ){ showLauncherFPS = !showLauncherFPS }
        )
    }
}

private fun VSH.createCategoryDebug() : XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_DEBUG,
        R.drawable.category_debug,
        R.string.settings_category_debug_name,
        R.string.settings_category_debug_desc
    )
}

private fun VSH.createCategoryDisplay() : XMBSettingsCategory {
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_DISPLAY,
        R.drawable.settings_category_display,
        R.string.settings_category_display_name,
        R.string.settings_category_display_desc
    ).apply {
        // region Layout Setting
        content.add(
            XMBSettingsItem(vsh,
                "setting_display_layout_0",
                R.string.setting_display_layout_type,
                R.string.setting_display_layout_type_desc,
                R.drawable.icon_orientation,
                {
                    when(xmbView?.state?.crossMenu?.layoutMode){
                        XMBLayoutType.PSP -> "PlayStation Portable"
                        XMBLayoutType.PS3 -> "PlayStation 3"
                        XMBLayoutType.PSX -> "PSX DVR"
                        XMBLayoutType.Bravia -> "Bravia TV"
                        else -> getString(R.string.unknown)
                    }
                },
            ) {
                val view = xmbView
                if(view != null){
                    view.state.crossMenu.layoutMode = when(view.state.crossMenu.layoutMode){
                        XMBLayoutType.PSP -> XMBLayoutType.Bravia
                        XMBLayoutType.Bravia -> XMBLayoutType.PS3
                        XMBLayoutType.PS3 -> XMBLayoutType.PSP
                        else -> XMBLayoutType.PS3
                    }
                }
            }
        )
        //endregion

        // region Analog Clock Second Settings
        content.add(
            XMBSettingsItem(vsh, "settings_display_analog_second",
                R.string.settings_display_clock_second_analog_name,
                R.string.settings_display_clock_second_analog_desc,
                R.drawable.icon_clock,
                { (xmbView?.state?.crossMenu?.statusBar?.secondOnAnalog == true).select(vsh.getString(R.string.common_yes),vsh.getString(R.string.common_no))  }
            ){
                val x = xmbView
                if(x != null){
                    x.state.crossMenu.statusBar.secondOnAnalog = !x.state.crossMenu.statusBar.secondOnAnalog
                }
            }
        )
        // endregion

        // region Show Operator Name Settings
        content.add(
            XMBSettingsItem(vsh, "settings_display_operator",
                R.string.settings_display_show_operator_name,
                R.string.settings_display_show_operator_desc,
                R.drawable.icon_network,
                { (xmbView?.state?.crossMenu?.statusBar?.showMobileOperator == true).select(vsh.getString(R.string.common_yes),vsh.getString(R.string.common_no))  }
            ){
                val x = xmbView
                if(x != null){
                    x.state.crossMenu.statusBar.showMobileOperator = !x.state.crossMenu.statusBar.showMobileOperator
                }
            }
        )
        // endregion
    }
}

private fun VSH.createCategoryInfo() : XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_SYSTEMINFO,
        R.drawable.icon_info,
        R.string.setting_systeminfo_name,
        R.string.setting_systeminfo_desc,
    ).apply {
        content.add(
            XMBSettingsItem(vsh, "systeminfo_android_version",
                R.string.string_systeminfo_androidver_name,
                R.string.empty_string,
                R.drawable.icon_android, { Build.VERSION.RELEASE }){ }
        )
        content.add(
            XMBSettingsItem(vsh, "systeminfo_android_model",
                R.string.string_systeminfo_androidmdl_name,
                R.string.empty_string,
                R.drawable.icon_android, { "${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})" }){ }
        )
    }
}