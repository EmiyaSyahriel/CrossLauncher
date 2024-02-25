package id.psw.vshlauncher.submodules.settings

import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem

class MediaSettings(private val vsh: Vsh) : ISettingsCategories(vsh) {
    private fun mkItemRequestMediaPermission() : XmbSettingsItem {
        val text = {
            vsh.getString(M.media.hasPermissionCached.select(R.string.permission_granted, R.string.permission_not_granted))
        }

        return XmbSettingsItem(vsh, "settings_media_request_permission",
            R.string.settings_media_request_permission_name,
            R.string.settings_media_request_permission_desc,
            R.drawable.icon_device_info,
            text, M.media::requestPermission)
    }

    override fun createCategory(): XmbSettingsCategory {
        return XmbSettingsCategory(vsh, SettingsSubmodule.CATEGORY_SETTINGS_MEDIA,
            R.drawable.icon_video_anim_icon,
            R.string.settings_category_media_name,
            R.string.settings_category_media_desc )
            .apply {
                content.add(mkItemRequestMediaPermission())
            }
    }
}