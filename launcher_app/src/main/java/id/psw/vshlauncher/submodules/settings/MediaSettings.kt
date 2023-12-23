package id.psw.vshlauncher.submodules.settings

import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbSettingsCategory

class MediaSettings(private val vsh: Vsh) : ISettingsCategories(vsh) {
    override fun createCategory(): XmbSettingsCategory {
        return XmbSettingsCategory(vsh, SettingsSubmodule.CATEGORY_SETTINGS_MEDIA,
            R.drawable.icon_video_anim_icon,
            R.string.settings_category_media_name,
            R.string.settings_category_media_desc )
            .apply {

            }
    }
}