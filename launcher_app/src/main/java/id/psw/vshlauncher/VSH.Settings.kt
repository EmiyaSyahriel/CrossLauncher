package id.psw.vshlauncher

import android.content.pm.ActivityInfo
import android.os.Build
import id.psw.vshlauncher.submodules.GamepadSubmodule
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.*
import id.psw.vshlauncher.views.XMBLayoutType
import id.psw.vshlauncher.views.XmbView
import id.psw.vshlauncher.views.dialogviews.TestDialogView
import id.psw.vshlauncher.views.showDialog
import java.util.*
import kotlin.collections.ArrayList

object SettingsCategoryID {
    const val CATEGORY_SETTINGS_WAVE = "settings_category_wave"
    const val CATEGORY_SETTINGS_ANDROID = "settings_category_android"
    const val CATEGORY_SETTINGS_DISPLAY = "settings_category_display"
    const val CATEGORY_SETTINGS_AUDIO = "settings_category_audio"
    const val CATEGORY_SETTINGS_DEBUG = "settings_category_debug"
    const val CATEGORY_SETTINGS_SYSTEMINFO = "settings_category_systeminfo"
    const val CATEGORY_SETTINGS_SYSTEM = "settings_category_system"
}

fun VSH.fillSettingsCategory(){
    threadPool.execute {
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryDisplay())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryAudio())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategorySystem())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryInfo())
        addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryAndroidSetting())

        if(BuildConfig.DEBUG){
            addToCategory(VSH.ITEM_CATEGORY_SETTINGS, createCategoryDebug())
        }
    }
}

private fun VSH.getCurrentLocaleName() : String {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        resources.configuration.locales[0].displayName
    }else{
        resources.configuration.locale.displayName
    }
}

private fun VSH.createCategoryAudio(): XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this, SettingsCategoryID.CATEGORY_SETTINGS_AUDIO,
        R.drawable.icon_volume,
        R.string.settings_category_audio_name, R.string.settings_category_audio_title).apply {

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

        content.add(XMBSettingsItem(vsh, "settings_system_orientation",
            R.string.item_orientation,
            R.string.orient_user, R.drawable.icon_orientation, {
                val xmb = xmbView?.context?.xmb
                getString(when(xmb?.requestedOrientation){
                    ActivityInfo.SCREEN_ORIENTATION_USER -> R.string.orient_user
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> R.string.orient_landscape
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> R.string.orient_portrait
                    else -> R.string.orient_unknown
                })
            }){
            val xmb = xmbView?.context?.xmb
            xmb?.requestedOrientation = when(xmb?.requestedOrientation){
                ActivityInfo.SCREEN_ORIENTATION_USER -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_USER
                else -> ActivityInfo.SCREEN_ORIENTATION_USER
            }
            pref.edit().putInt(PrefEntry.DISPLAY_ORIENTATION, xmb?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_SENSOR).apply()
        }.apply {
            val xmb = xmbView?.context?.xmb
            hasMenu = true
            val dMenuItems = arrayListOf<XMBMenuItem>()
            arrayOf(
                ActivityInfo.SCREEN_ORIENTATION_USER,
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).forEachIndexed { i, it ->
                val nameStr = getString(when(it){
                    ActivityInfo.SCREEN_ORIENTATION_USER -> R.string.orient_user
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> R.string.orient_landscape
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> R.string.orient_portrait
                    else -> R.string.orient_unknown
                })
                dMenuItems.add(XMBMenuItem.XMBMenuItemLambda({nameStr}, {false}, i){
                    xmb?.requestedOrientation = it
                })
            }
            menuItems = dMenuItems
        })

        content.add(XMBSettingsItem(vsh, "settings_system_button",
        R.string.settings_system_asian_console_name, R.string.settings_system_asian_console_desc,
        R.drawable.category_games, {
                getString(
                    if(GamepadSubmodule.Key.spotMarkedByX)
                        R.string.settings_system_asian_console_false
                    else
                        R.string.settings_system_asian_console_true
                )
            }
        ){
            GamepadSubmodule.Key.spotMarkedByX = !GamepadSubmodule.Key.spotMarkedByX
            pref.edit().putInt(PrefEntry.CONFIRM_BUTTON,
                GamepadSubmodule.Key.spotMarkedByX.select(1,0)).apply()
        })

        content.add(XMBSettingsItem(vsh, "settings_system_epimsg_disable",
            R.string.settings_system_disable_splash_message_title,
            R.string.settings_system_disable_splash_message_desc,
            R.drawable.icon_info, {
                getString(xmbView?.state?.coldBoot?.hideEpilepsyWarning?.select(
                    R.string.common_yes, R.string.common_no
                ) ?: R.string.unknown)
            })
        {
            val xv = xmbView
            if(xv != null){
                xv.state.coldBoot.hideEpilepsyWarning = !xv.state.coldBoot.hideEpilepsyWarning
                pref.edit()
                    .putInt(
                        PrefEntry.DISABLE_EPILEPSY_WARNING,
                        xv.state.coldBoot.hideEpilepsyWarning.select(1,0)
                    ).apply()
            }
        }
        )


        content.add(
            XMBSettingsItem(vsh, "settings_system_disable_video_icon",
                R.string.settings_system_disable_video_icon_name,
                R.string.settings_system_disable_video_icon_desc,
                R.drawable.category_video,
                {
                    getString(XMBAppItem.disableAnimatedIcon.select(R.string.common_yes, R.string.common_no))
                }
            ){
                XMBAppItem.disableAnimatedIcon = !XMBAppItem.disableAnimatedIcon
                pref.edit().putInt(
                    PrefEntry.DISPLAY_VIDEO_ICON,
                    XMBAppItem.disableAnimatedIcon.select(0,1)
                ).apply()
            }
        )

        val cal = Calendar.getInstance()
        val mon = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)
        if(mon == 3 && day == 1){
            content.add(XMBSettingsItem(vsh, "settings_what_is_it",
                R.string.settings_system_test_option_name,
                R.string.settings_system_test_option_desc,
                R.drawable.icon_developer, {
                    val i = xmbView?.keygenActive?.select(
                        R.string.settings_system_test_option_value_on,
                        R.string.settings_system_test_option_value_off) ?: R.string.empty_string
                    getString(i)
                }
            ){
                xmbView?.keygenActive = true
            })
        }
    }
}

private fun VSH.createCategoryDebug() : XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_DEBUG,
        R.drawable.category_debug,
        R.string.settings_category_debug_name,
        R.string.settings_category_debug_desc
    ).apply {

        content.add(XMBSettingsItem(vsh, "dbg_launch_dialog_test",
            R.string.dbg_launch_dialog_test, R.string.empty_string, R.drawable.category_debug, {""}){
            xmbView?.showDialog(TestDialogView(vsh))
        })

        content.add(XMBSettingsItem(vsh, "settings_system_language",
            R.string.settings_system_language, R.string.settings_system_language_description,
            R.drawable.icon_language, { getCurrentLocaleName() }
        ) {
            vsh.xmbView?.state?.itemMenu?.isDisplayed = true
        }.apply {
            hasMenu = true
            val dMenuItems = arrayListOf<XMBMenuItem>()
            supportedLocaleList.forEachIndexed { i, it ->
                val item = XMBMenuItem.XMBMenuItemLambda(
                    { it?.displayName ?: "System Default" },
                    { false }, i)
                {
                    vsh.setActiveLocale(it)
                }
                dMenuItems.add(item)
            }
            menuItems = dMenuItems
        })

    }
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
                    saveLayoutSetting()
                }
            }.apply{
                hasMenu = true
                val dMenu = ArrayList<XMBMenuItem>()

                    dMenu.add(XMBMenuItem.XMBMenuItemLambda(
                        {"PlayStation Portable"}, {false}, 1)
                    {
                        xmbView?.state?.crossMenu?.layoutMode = XMBLayoutType.PSP
                        saveLayoutSetting()
                    })
                    dMenu.add(XMBMenuItem.XMBMenuItemLambda(
                        {"PlayStation 3"}, {false}, 0)
                    {
                        xmbView?.state?.crossMenu?.layoutMode = XMBLayoutType.PS3
                        saveLayoutSetting()
                    })
                    dMenu.add(XMBMenuItem.XMBMenuItemLambda(
                        {"Bravia TV"}, {false}, 2)
                    {
                        xmbView?.state?.crossMenu?.layoutMode = XMBLayoutType.Bravia
                        saveLayoutSetting()
                    })
                menuItems = dMenu
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

        content.add(createCategoryWaveSetting())
    }
}

fun VSH.createCategoryWaveSetting(): XMBSettingsCategory {
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_WAVE,
        R.drawable.category_shortcut,
        R.string.title_activity_wave_wallpaper_setting,
        R.string.empty_string
    ).apply {

    }
}

fun VSH.saveLayoutSetting() {
    val view = xmbView
    if(view != null) {
        val srlzLayout = when (view.state.crossMenu.layoutMode) {
            XMBLayoutType.PS3 -> 0
            XMBLayoutType.PSP -> 1
            XMBLayoutType.Bravia -> 2
            XMBLayoutType.PSX -> 3
            else -> 0
        }
        pref.edit().putInt(PrefEntry.MENU_LAYOUT, srlzLayout).apply()
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

private fun VSH.createCategoryAndroidSetting() : XMBSettingsCategory{
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_ANDROID,
        R.drawable.icon_android,
        R.string.setting_android_name,
        R.string.setting_android_desc,
    ).apply {

        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.category_setting,
            R.string.android_sys_setting_homepage_name,
            R.string.android_sys_setting_homepage_desc,
            android.provider.Settings.ACTION_SETTINGS
        ))


        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_network,
            R.string.android_sys_network_name,
            R.string.android_sys_network_desc,
            android.provider.Settings.ACTION_WIRELESS_SETTINGS
        ))

        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_cda,
            R.string.android_sys_devices_name,
            R.string.android_sys_devices_desc,
            android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        ))

        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.category_apps,
            R.string.android_sys_apps_name,
            R.string.android_sys_apps_desc,
            "id.psw.vshlauncher.settings.category.apps"
        ).apply {
            hasContent = true
            content.add(
                XMBAndroidSettingShortcutItem(
                    vsh, R.drawable.category_apps,
                    R.string.android_sys_all_apps_name,  R.string.android_sys_all_apps_desc,
                    android.provider.Settings.ACTION_APPLICATION_SETTINGS
                )
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                content.add(
                    XMBAndroidSettingShortcutItem(
                        vsh, R.drawable.category_notifications,
                        R.string.android_sys_nodisturb_name, R.string.android_sys_nodisturb_desc,
                        android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    )
                )
                content.add(
                    XMBAndroidSettingShortcutItem(
                        vsh, R.drawable.category_notifications,
                        R.string.android_sys_notification_name, R.string.android_sys_notification_desc,
                        android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                content.add(
                    XMBAndroidSettingShortcutItem(
                        vsh, R.drawable.category_apps,
                        R.string.android_sys_default_apps_name,  R.string.android_sys_default_apps_desc,
                        android.provider.Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                    )
                )
            }

        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            content.add(XMBAndroidSettingShortcutItem(
                vsh, R.drawable.icon_battery,
                R.string.android_sys_battery_name,
                R.string.android_sys_battery_desc,
                android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS
            ))
        }
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_brightness,
            R.string.android_sys_display_name,
            R.string.android_sys_display_desc,
            android.provider.Settings.ACTION_DISPLAY_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_volume,
            R.string.android_sys_sound_name,
            R.string.android_sys_sound_desc,
            android.provider.Settings.ACTION_SOUND_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_storage,
            R.string.android_sys_storage_name,
            R.string.android_sys_storage_desc,
            android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_privacy,
            R.string.android_sys_privacy_name,
            R.string.android_sys_privacy_desc,
            android.provider.Settings.ACTION_PRIVACY_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_location,
            R.string.android_sys_location_name,
            R.string.android_sys_location_desc,
            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_security,
            R.string.android_sys_security_name,
            R.string.android_sys_security_desc,
            android.provider.Settings.ACTION_SECURITY_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_accessibility,
            R.string.android_sys_accessibility_name,
            R.string.android_sys_accessibility_desc,
            android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS
        ))
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_info,
            R.string.android_sys_systeminfo_name,
            R.string.android_sys_systeminfo_desc,
            "id.psw.vshlauncher.settings.category.system"
        ).apply {
            hasContent = true
            content.add(XMBAndroidSettingShortcutItem(
                vsh, R.drawable.icon_language,
                R.string.android_sys_locale_name,
                R.string.android_sys_locale_desc,
                android.provider.Settings.ACTION_LOCALE_SETTINGS
            ))

            content.add(XMBAndroidSettingShortcutItem(
                vsh, R.drawable.icon_datetime,
                R.string.android_sys_datetime_name,
                R.string.android_sys_datetime_desc,
                android.provider.Settings.ACTION_DATE_SETTINGS
            ))

            content.add(XMBAndroidSettingShortcutItem(
                vsh, R.drawable.icon_developer,
                R.string.android_sys_developer_name,
                R.string.android_sys_developer_desc,
                android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
            ))
        })
        content.add(XMBAndroidSettingShortcutItem(
            vsh, R.drawable.icon_device_info,
            R.string.android_sys_deviceinfo_name,
            R.string.android_sys_deviceinfo_desc,
            android.provider.Settings.ACTION_DEVICE_INFO_SETTINGS
        ))
    }
}