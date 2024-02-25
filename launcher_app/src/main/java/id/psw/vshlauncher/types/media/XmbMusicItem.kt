package id.psw.vshlauncher.types.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.submodules.BitmapManager
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.items.XmbMenuItem
import java.io.File

class XmbMusicItem(private val vsh: Vsh, val data : MusicData) : XmbItem(vsh) {
    companion object {
        private var noAlbum : BitmapRef? = null
    }

    override val displayName: String
        get() = data.title

    override val id: String = "XMB_MUSIC_${data.id}"

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() = "${data.album} - ${data.artist}"

    private val defaultBitmap = BitmapRef("none", { TRANSPARENT_BITMAP }, BitmapRef.FallbackColor.Transparent)

    private var _icon : BitmapRef = defaultBitmap

    private val _itemMenus = arrayListOf<XmbMenuItem>()

    override val isIconLoaded: Boolean
        get() = _icon.isLoaded

    override val hasIcon: Boolean
        get() = true
    override val icon: Bitmap
        get() = _icon.bitmap

    override val hasMenu: Boolean
        get() = true

    override val menuItems: ArrayList<XmbMenuItem>?
        get() = _itemMenus

    override val menuItemCount: Int
        get() = _itemMenus.size

    private fun iconLoader() : Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(data.data)
        val dat = mmr.embeddedPicture

        /// TODO: Load No_Album icon only once
        return if(dat == null){
            val bmp = vsh.resources.getDrawable(R.drawable.ic_music_no_album).toBitmap(300,300)
            bmp
        } else {
            val bmp = BitmapFactory.decodeByteArray(dat, 0, dat.size)
            bmp
        }
    }

    private fun loadIcon(i:XmbItem){
        _icon = BitmapRef("albumart_$id", ::iconLoader)
    }

    private fun unloadIcon(i:XmbItem){
        if(_icon != defaultBitmap) _icon.release()
        _icon = defaultBitmap
    }

    private fun launch(i:XmbItem){
        vsh.openFileOnExternalApp(File(data.data))
    }

    init {
        vsh.M.media.createMediaMenuItems(_itemMenus, data)
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launch

    override val onScreenVisible: (XmbItem) -> Unit
        get() = ::loadIcon

    override val onScreenInvisible: (XmbItem) -> Unit
        get() = ::unloadIcon
}