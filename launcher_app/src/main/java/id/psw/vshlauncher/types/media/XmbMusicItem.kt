package id.psw.vshlauncher.types.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.types.XmbItem
import java.io.File

class XmbMusicItem(private val vsh: Vsh, val data : MusicData) : XmbItem(vsh) {
    override val id: String = "MUSIC_INTERNAL_${data.id}"
    override val displayName: String
        get() = data.title

    override val description: String
        get() = "${data.album} - ${data.artist}"

    private var _hasIcon = false
    private var _icon : Bitmap? = null
    private var _hasIconLoaded = false

    override val isIconLoaded: Boolean
        get() = _hasIconLoaded

    override val hasIcon: Boolean
        get() = _hasIcon
    override val icon: Bitmap
        get() = _icon!!

    private fun loadIcon(i:XmbItem){
        vsh.threadPool.execute {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(data.data)
            val dat = mmr.embeddedPicture
            if(dat == null){
                _hasIcon = false
            }else{
                _icon = BitmapFactory.decodeByteArray(dat, 0, dat.size)
                _hasIcon = _icon != null
                _hasIconLoaded = true
            }
        }
    }

    private fun unloadIcon(i:XmbItem){
        _hasIcon = false
        if(_icon != null){
            _hasIconLoaded = false
            _icon?.recycle()
            _icon = null
        }
    }

    private fun launch(i:XmbItem){
        vsh.openFileByDefaultApp(File(data.data))
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launch

    override val onScreenVisible: (XmbItem) -> Unit
        get() = ::loadIcon

    override val onScreenInvisible: (XmbItem) -> Unit
        get() = ::unloadIcon
}