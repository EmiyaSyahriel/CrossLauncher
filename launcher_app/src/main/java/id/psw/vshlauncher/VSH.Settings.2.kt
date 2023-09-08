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
import id.psw.vshlauncher.types.XMBItem
import id.psw.vshlauncher.types.items.XMBMenuItem
import id.psw.vshlauncher.types.items.XMBSettingsCategory
import id.psw.vshlauncher.types.items.XMBSettingsItem
import id.psw.vshlauncher.views.VshViewPage
import id.psw.vshlauncher.views.dialogviews.LegacyIconBackgroundDialogView
import id.psw.vshlauncher.views.dialogviews.TextDialogView
import id.psw.vshlauncher.views.showDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                vsh.postNotification(R.drawable.category_setting,
                    getString(R.string.settings_common_settings_unavailable),
                    getString(R.string.settings_wave_live_wallpaper_unsupported)
                )
            }
        })

        content.add(XMBSettingsItem(vsh, "settings_wave_make_internal",
            R.string.settings_wave_make_internal_name, R.string.setting_wave_make_internal_desc,
            R.drawable.category_settings_display, {
                getString(vsh.useInternalWave.select(R.string.common_yes, R.string.common_no))
            }
        ){
            xmbView?.showDialog(
                TextDialogView(vsh).setData(null, getString(R.string.common_reboot_required),
                    getString(R.string.settings_wave_apply_as_layer_dlg_text_main))
                    .setPositive(getString(R.string.common_reboot)){_ ->
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
                    .setNegative(getString(android.R.string.cancel)){dlg -> dlg.finish(VshViewPage.MainMenu) }
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

fun VSH.settingsAddInstallPackage(): XMBItem {
    val xi = XMBSettingsItem(this, "settings_install_package", R.string.settings_install_package, R.string.empty_string, R.drawable.ic_folder,{
        ""
    }){
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
        i.type = "*/*"
        try{
            xmbView?.context?.xmb?.startActivityForResult(Intent.createChooser(i, getString(R.string.settings_install_package)), VSH.ACT_REQ_INSTALL_PACKAGE)
        }catch(e:Exception){}

    }
    return xi
}

fun VSH.setSysBarVisibility(i:Int){
    vsh.xmb.sysBarVisibility = i
    M.pref.set(PrefEntry.SYSTEM_STATUS_BAR, i)
    vsh.xmb.updateSystemBarVisibility()
}

fun VSH.settingsAddSystemSetting2(cat : XMBSettingsCategory){
    cat.content.add(XMBSettingsItem(vsh, "settings_system_android_bar",
        R.string.settings_system_android_bar_name,
        R.string.settings_system_android_bar_desc,
        R.drawable.icon_hidden,
        {
            vsh.getString(when(vsh.xmb.sysBarVisibility){
                SysBar.ALL -> R.string.system_bar_visible_all
                SysBar.NAVIGATION -> R.string.system_bar_visible_navigation
                SysBar.STATUS -> R.string.system_bar_visible_status
                SysBar.NONE -> R.string.system_bar_visible_none
                else -> R.string.unknown
            })
        }
    ){
        val i = when(vsh.xmb.sysBarVisibility){
            SysBar.ALL -> SysBar.STATUS
            SysBar.STATUS -> SysBar.NAVIGATION
            SysBar.NAVIGATION -> SysBar.NONE
            SysBar.NONE -> SysBar.ALL
            else -> SysBar.NONE
        }
        setSysBarVisibility(i)
    }.apply {
        val menu = arrayListOf<XMBMenuItem>()
        arrayListOf(
            R.string.system_bar_visible_all to SysBar.ALL,
            R.string.system_bar_visible_navigation to SysBar.NAVIGATION,
            R.string.system_bar_visible_status to SysBar.STATUS,
            R.string.system_bar_visible_none to SysBar.NONE
        ).forEachIndexed { i, it ->
            menu.add(XMBMenuItem.XMBMenuItemLambda({ getString(it.first) }, { false }, i){
                setSysBarVisibility(it.second)
                xmb.xmbView.state.itemMenu.isDisplayed = false
            })
        }
        menuItems = menu
        hasMenu = true
    })

    cat.content.add(XMBSettingsItem(vsh, "settings_system_legacy_icon_bg", R.string.dlg_legacyicon_title, R.string.settings_system_legacy_icon_background_desc, R.drawable.icon_video_anim_icon, {
        val mode = M.pref.get(PrefEntry.ICON_RENDERER_LEGACY_BACKGROUND, 0)
        vsh.getString(when(mode){
            1 -> R.string.common_enabled
            2 -> R.string.dlg_legacyicon_material_you
            else -> R.string.common_disabled
        })
    }){
        vsh.xmbView?.showDialog(LegacyIconBackgroundDialogView(vsh))
    })
}