package id.psw.vshlauncher.submodules.settings

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import id.psw.vshlauncher.PrefEntry
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addAllV
import id.psw.vshlauncher.livewallpaper.XMBWaveRenderer
import id.psw.vshlauncher.livewallpaper.XMBWaveSettingSubDialog
import id.psw.vshlauncher.livewallpaper.XMBWaveSurfaceView
import id.psw.vshlauncher.livewallpaper.XMBWaveWallpaperService
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.select
import id.psw.vshlauncher.submodules.SettingsSubmodule
import id.psw.vshlauncher.types.items.XmbSettingsCategory
import id.psw.vshlauncher.types.items.XmbSettingsItem
import id.psw.vshlauncher.views.dialogviews.TextDialogView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaveSettings(private val vsh: Vsh) : ISettingsCategories(vsh) {
    private var settingWaveCurrentWaveStyle = XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL

    private fun readWaveConfig(){
        vsh.getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE).apply {
            settingWaveCurrentWaveStyle = getInt(XMBWaveSurfaceView.KEY_STYLE, XMBWaveRenderer.WAVE_TYPE_PSP.toInt()).toByte()
        }
    }

    private fun updateWaveConfig(){
        vsh.getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE).edit(false) {
            putInt(XMBWaveSurfaceView.KEY_STYLE, settingWaveCurrentWaveStyle.toInt())
        }
        vsh.waveShouldReReadPreferences = true
    }

    private fun setWaveStyle(s:Byte){
        settingWaveCurrentWaveStyle = s
        updateWaveConfig()
    }

    fun showXMBLiveWallpaperWizard(){
        val xv = vsh.xmbView
        xv?.showDialog(XMBWaveSettingSubDialog(xv))
    }

    private fun mkItemSetLiveWallpaper() : XmbSettingsItem {
        val click : () -> Unit = {
            try{
                val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                val pkg = vsh.packageName
                val cls = XMBWaveWallpaperService::class.java.canonicalName ?:
                "id.psw.vshlauncher.livewallpaper.XMBWaveWallpaperService"
                i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(pkg, cls))
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                vsh.startActivity(i)
            }catch (e:Exception){
                e.printStackTrace()
                vsh.postNotification(
                    R.drawable.category_setting,
                    vsh.getString(R.string.settings_common_settings_unavailable),
                    vsh.getString(R.string.settings_wave_live_wallpaper_unsupported)
                )
            }
        }

        return XmbSettingsItem(vsh, "settings_wave_set",
            R.string.settings_wave_set_name, R.string.settings_wave_set_desc,
            R.drawable.category_setting, {""}, click)
    }

    private fun mkItemSetInternalWallpaper() : XmbSettingsItem {
        val text = {
            vsh.getString(vsh.useInternalWave.select(R.string.common_yes, R.string.common_no))
        }
        val click : () -> Unit = {
            val xv = vsh.xmbView
            xv?.showDialog(
                TextDialogView(xv).setData(null, vsh.getString(R.string.common_reboot_required),
                    vsh.getString(R.string.settings_wave_apply_as_layer_dlg_text_main))
                    .setPositive(vsh.getString(R.string.common_reboot)){ _ ->
                        // Commit instead of apply, allow the app to save the preference before restarting
                        M.pref
                            .set(PrefEntry.USES_INTERNAL_WAVE_LAYER, !vsh.useInternalWave)
                            .push()

                        vsh.lifeScope.launch {
                            withContext(Dispatchers.Default){
                                delay(1000L)
                                vsh.restart()
                            }
                        }
                    }
                    .setNegative(vsh.getString(android.R.string.cancel)){dlg -> dlg.finish(xv.screens.mainMenu) }
            )
        }
        return XmbSettingsItem(vsh, "settings_wave_make_internal",
            R.string.settings_wave_make_internal_name, R.string.setting_wave_make_internal_desc,
            R.drawable.category_settings_display, text, click)
    }

    private fun mkItemOpenWizard() : XmbSettingsItem {
        return XmbSettingsItem(vsh, "settings_wave_theme",
            R.string.settings_wave_theme_name,
            R.string.settings_wave_theme_desc,
            R.drawable.category_setting, { "" },
            ::showXMBLiveWallpaperWizard
        )
    }

    override fun createCategory(): XmbSettingsCategory {
        return XmbSettingsCategory(vsh,
            SettingsSubmodule.CATEGORY_SETTINGS_WAVE,
            R.drawable.category_shortcut,
            R.string.title_activity_wave_wallpaper_setting,
            R.string.empty_string
        ).apply {
            content.addAllV(
                mkItemSetLiveWallpaper(),
                mkItemSetInternalWallpaper(),
                mkItemOpenWizard())

        }
    }
}