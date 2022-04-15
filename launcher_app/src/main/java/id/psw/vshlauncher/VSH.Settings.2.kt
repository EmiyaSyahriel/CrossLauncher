package id.psw.vshlauncher

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import id.psw.vshlauncher.livewallpaper.XMBWaveRenderer
import id.psw.vshlauncher.livewallpaper.XMBWaveSettingSubDialog
import id.psw.vshlauncher.livewallpaper.XMBWaveSurfaceView
import id.psw.vshlauncher.livewallpaper.XMBWaveWallpaperService
import id.psw.vshlauncher.types.items.XMBMenuItem
import id.psw.vshlauncher.types.items.XMBSettingsCategory
import id.psw.vshlauncher.types.items.XMBSettingsItem
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.dialogviews.TextDialogView
import id.psw.vshlauncher.views.showDialog


private var settingWaveCurrentWaveStyle = XMBWaveRenderer.WAVE_TYPE_PS3_NORMAL
private fun VSH.updateWaveConfig(){
    getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE).edit(false) {
        putInt(XMBWaveSurfaceView.KEY_STYLE, settingWaveCurrentWaveStyle.toInt())
    }
    waveShouldReReadPreferences = true
}

private fun VSH.readWaveConfig(){
    getSharedPreferences(XMBWaveSurfaceView.PREF_NAME, Context.MODE_PRIVATE).apply {
        settingWaveCurrentWaveStyle = getInt(XMBWaveSurfaceView.KEY_STYLE, XMBWaveRenderer.WAVE_TYPE_PSP.toInt()).toByte()
    }
}

private fun VSH.setWaveStyle(s:Byte){
    settingWaveCurrentWaveStyle = s
    updateWaveConfig()
}

fun VSH.showXMBLiveWallpaperWizard(){
    xmbView?.showDialog(XMBWaveSettingSubDialog(vsh))
}

@SuppressLint("ApplySharedPref")
fun VSH.createCategoryWaveSetting(): XMBSettingsCategory {
    val vsh = this
    return XMBSettingsCategory(this,
        SettingsCategoryID.CATEGORY_SETTINGS_WAVE,
        R.drawable.category_shortcut,
        R.string.title_activity_wave_wallpaper_setting,
        R.string.empty_string
    ).apply {
        content.add(XMBSettingsItem(vsh, "settings_wave_set",
            R.string.settings_wave_set_name, R.string.settings_wave_set_desc,
            R.drawable.category_setting, {""}
        ){
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
                vsh.postNotification(R.drawable.category_setting, "Setting unavailable","This device probably didn't support live wallpaper")
            }
        })

        content.add(XMBSettingsItem(vsh, "settings_wave_make_internal",
            R.string.settings_wave_make_internal_name, R.string.setting_wave_make_internal_desc,
            R.drawable.category_settings_display, {
                getString(vsh.useInternalWave.select(R.string.common_yes, R.string.common_no))
            }
        ){
            xmbView?.showDialog(
                TextDialogView(vsh).setData(null, "Reboot Required",
                    "Changing the usage of Internal Wave Layer requires the launcher to be rebooted.\n"+
                            "Would you like to change the value and reboot?")
                    .setPositive("Reboot"){_ ->
                        // Commit instead of apply, allow the app to save the preference before restarting
                        pref.edit().putBoolean(PrefEntry.USES_INTERNAL_WAVE_LAYER, !vsh.useInternalWave).commit()
                        vsh.restart()
                    }
                    .setNegative("Cancel"){dlg -> dlg.finish(VshViewPage.MainMenu) }
            )
        })

        content.add(
            XMBSettingsItem(vsh, "settings_wave_theme",
                R.string.settings_wave_theme_name,
                R.string.settings_wave_theme_desc,
                R.drawable.category_setting, { "" }
            ){
                showXMBLiveWallpaperWizard()
            }
        )

    }
}
