package id.psw.vshlauncher

import android.content.Context
import android.graphics.BitmapFactory
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.views.VshView

class VshCategory(var context: VSH, vsh:VshView, private val iconId : String) : XMBIcon(iconId) {
    companion object {
        const val apps = "explore_category_apps"
        const val settings = "explore_category_sysconf"
        const val home = "explore_category_home"
        const val video = "explore_category_video"
        const val photo = "explore_category_photo"
        const val music = "explore_category_music"
        const val games = "explore_category_game"
    }
}