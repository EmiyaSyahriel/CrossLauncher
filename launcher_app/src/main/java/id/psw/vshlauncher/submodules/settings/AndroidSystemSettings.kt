package id.psw.vshlauncher.submodules.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addAllV
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbAndroidSettingShortcutItem
import id.psw.vshlauncher.types.items.XmbSettingsCategory

class AndroidSystemSettings(private val vsh: Vsh) : ISettingsCategories(vsh) {
    private fun createCategoryApps() : XmbAndroidSettingShortcutItem{
        return XmbAndroidSettingShortcutItem(
            vsh, R.drawable.category_apps,
            R.string.android_sys_apps_name,
            R.string.android_sys_apps_desc,
            "id.psw.vshlauncher.settings.category.apps"
        ).apply {
            hasContent = true
            content.add(
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.category_apps,
                    R.string.android_sys_all_apps_name,  R.string.android_sys_all_apps_desc,
                    Settings.ACTION_APPLICATION_SETTINGS
                )
            )
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                content.addAllV(
                    XmbAndroidSettingShortcutItem(
                        vsh, R.drawable.category_notifications,
                        R.string.android_sys_nodisturb_name, R.string.android_sys_nodisturb_desc,
                        Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                    ),
                    XmbAndroidSettingShortcutItem(
                        vsh, R.drawable.category_notifications,
                        R.string.android_sys_notification_name, R.string.android_sys_notification_desc,
                        Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                content.add(
                    XmbAndroidSettingShortcutItem(
                        vsh, R.drawable.category_apps,
                        R.string.android_sys_default_apps_name,  R.string.android_sys_default_apps_desc,
                        Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                    )
                )
            }

        }
    }

    private fun createCategorySystem() : XmbAndroidSettingShortcutItem {
        return XmbAndroidSettingShortcutItem(
            vsh, R.drawable.icon_info,
            R.string.android_sys_systeminfo_name,
            R.string.android_sys_systeminfo_desc,
            "id.psw.vshlauncher.settings.category.system"
        ).apply {
            hasContent = true
            content.addAllV(
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_language,
                    R.string.android_sys_locale_name,
                    R.string.android_sys_locale_desc,
                    Settings.ACTION_LOCALE_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_datetime,
                    R.string.android_sys_datetime_name,
                    R.string.android_sys_datetime_desc,
                    Settings.ACTION_DATE_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_developer,
                    R.string.android_sys_developer_name,
                    R.string.android_sys_developer_desc,
                    Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                )
            )
        }
    }

    override fun createCategory(): XmbSettingsCategory {
        return XmbSettingsCategory(vsh,
            SettingsSubmodule.CATEGORY_SETTINGS_ANDROID,
            R.drawable.icon_android,
            R.string.setting_android_name,
            R.string.setting_android_desc,
        ).apply {

            content.addAllV(
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.category_setting,
                    R.string.android_sys_setting_homepage_name,
                    R.string.android_sys_setting_homepage_desc,
                    Settings.ACTION_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_network,
                    R.string.android_sys_network_name,
                    R.string.android_sys_network_desc,
                    Settings.ACTION_WIRELESS_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_cda,
                    R.string.android_sys_devices_name,
                    R.string.android_sys_devices_desc,
                    Settings.ACTION_BLUETOOTH_SETTINGS
                ),
                createCategoryApps()
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                content.add(
                    XmbAndroidSettingShortcutItem(
                        vsh, R.drawable.icon_battery,
                        R.string.android_sys_battery_name,
                        R.string.android_sys_battery_desc,
                        Settings.ACTION_BATTERY_SAVER_SETTINGS
                    )
                )
            }
            content.addAllV(
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_brightness,
                    R.string.android_sys_display_name,
                    R.string.android_sys_display_desc,
                    Settings.ACTION_DISPLAY_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_color,
                    R.string.android_sys_wallpaper_name,
                    R.string.android_sys_wallpaper_desc,
                    Intent.ACTION_SET_WALLPAPER
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_volume,
                    R.string.android_sys_sound_name,
                    R.string.android_sys_sound_desc,
                    Settings.ACTION_SOUND_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_storage,
                    R.string.android_sys_storage_name,
                    R.string.android_sys_storage_desc,
                    Settings.ACTION_INTERNAL_STORAGE_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_privacy,
                    R.string.android_sys_privacy_name,
                    R.string.android_sys_privacy_desc,
                    Settings.ACTION_PRIVACY_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_location,
                    R.string.android_sys_location_name,
                    R.string.android_sys_location_desc,
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_security,
                    R.string.android_sys_security_name,
                    R.string.android_sys_security_desc,
                    Settings.ACTION_SECURITY_SETTINGS
                ),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_accessibility,
                    R.string.android_sys_accessibility_name,
                    R.string.android_sys_accessibility_desc,
                    Settings.ACTION_ACCESSIBILITY_SETTINGS
                ),
                createCategorySystem(),
                XmbAndroidSettingShortcutItem(
                    vsh, R.drawable.icon_device_info,
                    R.string.android_sys_deviceinfo_name,
                    R.string.android_sys_deviceinfo_desc,
                    Settings.ACTION_DEVICE_INFO_SETTINGS
                )
            )
        }
    }
}