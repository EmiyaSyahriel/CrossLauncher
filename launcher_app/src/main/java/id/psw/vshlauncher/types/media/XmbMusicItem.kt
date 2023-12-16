package id.psw.vshlauncher.types.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.XmbItem
import java.io.File

class XmbMusicItem(val vsh: Vsh, val data : MusicData) : XmbItem(vsh) {
    override val displayName: String
        get() = data.title

    override val id: String = "XMB_MUSIC_${data.id}"

    override val hasDescription: Boolean
        get() = true

    override val description: String
        get() = "${data.album} - ${data.artist}"

    private var _hasIcon = false
    private var _icon : Bitmap? = null

    private var _isIconLoaded = false

    override val isIconLoaded: Boolean
        get() = _isIconLoaded
    override val hasIcon: Boolean
        get() = _hasIcon
    override val icon: Bitmap
        get() = _icon!!

    private fun loadIcon(i:XmbItem){
        vsh.threadPool.execute {
            val that = this
            that._isIconLoaded = false
            that._icon = if (sdkAtLeast(Build.VERSION_CODES.Q)) {
                vsh.contentResolver.loadThumbnail(data.uri, Size(320, 176), null)
            } else {
                MediaStore.Images.Thumbnails.getThumbnail(vsh.contentResolver, data.id, MediaStore.Images.Thumbnails.MINI_KIND, null)
            }
            that._isIconLoaded = true
            that._hasIcon = that._icon != null
        }
    }

    private fun unloadIcon(i:XmbItem){
        _hasIcon = false
        if(_icon != null){
            _icon?.recycle()
            _icon = null
        }
    }

    private fun launch(i:XmbItem){
        // Open by default or use internal device
        vsh.openFileByDefaultApp(File(data.data))
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launch

    override val onScreenVisible: (XmbItem) -> Unit
        get() = ::loadIcon

    override val onScreenInvisible: (XmbItem) -> Unit
        get() = ::unloadIcon
}