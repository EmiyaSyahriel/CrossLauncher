package id.psw.vshlauncher.submodules

import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.addToCategory
import id.psw.vshlauncher.submodules.settings.AndroidSystemSettings
import id.psw.vshlauncher.submodules.settings.AudioSettings
import id.psw.vshlauncher.submodules.settings.DebugSettings
import id.psw.vshlauncher.submodules.settings.DisplaySettings
import id.psw.vshlauncher.submodules.settings.MediaSettings
import id.psw.vshlauncher.submodules.settings.RootDirSettings
import id.psw.vshlauncher.submodules.settings.SystemSettings
import kotlinx.coroutines.launch

class SettingsSubmodule(private val ctx : Vsh) : IVshSubmodule
{
    companion object {
        const val CATEGORY_SETTINGS_WAVE = "settings_category_wave"
        const val CATEGORY_SETTINGS_ANDROID = "settings_category_android"
        const val CATEGORY_SETTINGS_DISPLAY = "settings_category_display"
        const val CATEGORY_SETTINGS_MEDIA = "settings_category_media"
        const val CATEGORY_SETTINGS_AUDIO = "settings_category_audio"
        const val CATEGORY_SETTINGS_DEBUG = "settings_category_debug"
        const val CATEGORY_SETTINGS_SYSTEMINFO = "settings_category_systeminfo"
        const val CATEGORY_SETTINGS_SYSTEM = "settings_category_system"
    }

    val display         = DisplaySettings(ctx)
    private val audio   = AudioSettings(ctx)
    private val system  = SystemSettings(ctx)
    private val media   = MediaSettings(ctx)
    private val android = AndroidSystemSettings(ctx)
    private val debug   = DebugSettings(ctx)
    private val rootDir = RootDirSettings(ctx)
    fun fillSettings(){
        ctx.lifeScope.launch {
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, display.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, audio.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, media.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, system.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, android.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, rootDir.settingsAddSystemUpdate())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, debug.createCategory())
            ctx.addToCategory(Vsh.ITEM_CATEGORY_SETTINGS, rootDir.settingsAddInstallPackage())

        }
    }

    override fun onCreate() {
    }

    override fun onDestroy() {

    }
}