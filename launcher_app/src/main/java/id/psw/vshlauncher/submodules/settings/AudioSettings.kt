package id.psw.vshlauncher.submodules.settings

import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addAllV
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem
import kotlin.math.roundToInt

class AudioSettings(private val vsh: Vsh) : ISettingsCategories(vsh) {
    private fun makeVolume(setting: XmbSettingsItem, volume : (Float) -> Unit){
        setting.hasMenu = true
        val v = arrayListOf<XmbMenuItem>()
        for(i in 10 downTo 0)
        {
            v.add(XmbMenuItem.XmbMenuItemLambda({ i.toString() }, {false}, (10 - i) - 5){
                volume.invoke(i / 10.0f)
                vsh.xmbView?.showSideMenu(false)
            })
        }
        setting.menuItems = v

    }

    private fun volumeToString(v: Float) : String = (v * 10).roundToInt().toString()

    private fun mkItemVolumeMaster(): XmbSettingsItem {
        return XmbSettingsItem(vsh, "audio_volume_master",
            R.string.settings_audio_master_volume_name,
            R.string.settings_audio_master_volume_desc,
            R.drawable.icon_volume, { volumeToString(vsh.M.audio.master) }
        ){
            vsh.xmbView?.showSideMenu(true)
        }.apply {
            makeVolume( this) { vsh.M.audio.master = it }
        }
    }

    private fun mkItemVolumeBgm() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "audio_volume_bgm",
            R.string.settings_audio_bgm_volume_name,
            R.string.settings_audio_bgm_volume_desc,
            R.drawable.category_music, { volumeToString(vsh.M.audio.bgm) }
        ){
            vsh.xmbView?.showSideMenu(true)
        }.apply {
            makeVolume( this) { vsh.M.audio.bgm = it }
        }
    }

    private fun mkItemVolumeSystemBgm() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "audio_volume_sysbgm",
            R.string.settings_audio_sysbgm_volume_name,
            R.string.settings_audio_sysbgm_volume_desc,
            R.drawable.ic_component_audio, { volumeToString(vsh.M.audio.systemBgm) }
        ){
            vsh.xmbView?.showSideMenu(true)
        }.apply {
            makeVolume( this) { vsh.M.audio.systemBgm = it }
        }
    }

    private fun mkItemVolumeSfx() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "audio_volume_sfx",
            R.string.settings_audio_sfx_volume_name,
            R.string.settings_audio_sfx_volume_desc,
            R.drawable.ic_speaker_phone, { volumeToString(vsh.M.audio.sfx) }
        ){
            vsh.xmbView?.showSideMenu(true)
        }.apply {
            makeVolume( this) { vsh.M.audio.sfx = it }
        }}

    private fun mkItemReloadSfx() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "audio_reload_sfx",
            R.string.settings_audio_sfx_reload_name,
            R.string.settings_audio_sfx_reload_desc,
            R.drawable.icon_refresh, { "" }
        ){
            vsh.M.audio.loadSfxData(false)
        }
    }

    override fun createCategory() : XmbSettingsCategory {
        return XmbSettingsCategory(vsh, SettingsSubmodule.CATEGORY_SETTINGS_AUDIO,
            R.drawable.icon_volume,
            R.string.settings_category_audio_name, R.string.settings_category_audio_title
        ).apply {
            content.addAllV(
                mkItemVolumeMaster(),
                mkItemVolumeBgm(),
                mkItemVolumeSystemBgm(),
                mkItemVolumeSfx(),
                mkItemReloadSfx()
            )
        }
    }

}