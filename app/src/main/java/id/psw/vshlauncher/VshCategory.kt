package id.psw.vshlauncher

import android.content.Context
import android.graphics.BitmapFactory
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.icontypes.XMBIcon
import id.psw.vshlauncher.views.VshView

class VshCategory(context: VSH, vsh:VshView, private val iconId : String) : XMBIcon(context, vsh, iconId) {
    companion object {
        const val apps = "explore_category_apps"
        const val settings = "explore_category_sysconf"
        const val home = "explore_category_home"
        const val video = "explore_category_video"
        const val photo = "explore_category_photo"
        const val music = "explore_category_music"
        const val games = "explore_category_game"

        val defNameIds = mapOf(
            Pair(apps, R.string.category_apps),
            Pair(settings, R.string.category_apps),
            Pair(home, R.string.category_home),
            Pair(video, R.string.category_videos),
            Pair(photo, R.string.category_videos),
            Pair(music, R.string.category_music),
            Pair(games, R.string.category_games)
        )

        val defIconIds = mapOf(
            Pair(apps, R.drawable.category_apps),
            Pair(settings,R.drawable.category_setting),
            Pair(home, R.drawable.category_home),
            Pair(video, R.drawable.category_video),
            Pair(photo, R.drawable.category_video),
            Pair(music, R.drawable.category_music),
            Pair(games, R.drawable.category_games)
        )
    }

    private lateinit var _icon : Icon
    init {
        val defaultIconId = defIconIds[iconId] ?: R.drawable.t_format_background
        if(vsh.isInEditMode){
            Icon.fromBitmap(BitmapFactory.decodeResource(context.resources, defaultIconId))
        }else{
            Icon.fromBitmap((context as VSH).loadLauncherCustomIcon(iconId, defaultIconId))
        }
    }

    override val name: String
        get() = context.resources.getString(defNameIds[iconId] ?: R.string.unknown_symbols)

    override val icon: Icon
        get() = _icon
}