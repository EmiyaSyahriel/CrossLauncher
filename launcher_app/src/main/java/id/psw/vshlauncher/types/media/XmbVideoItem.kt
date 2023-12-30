package id.psw.vshlauncher.types.media

import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.sdkAtLeast
import id.psw.vshlauncher.types.XmbItem
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.views.asBytes
import java.io.File

class XmbVideoItem(val vsh: Vsh, val data : VideoData) : XmbItem(vsh) {
    override val id: String = "VIDEO_INTERNAL_${data.id}"
    override val displayName: String
        get() = data.displayName

    override val description: String
        get() = data.size.asBytes()

    private var _hasIcon = false
    private var _icon : Bitmap? = null

    private var _isIconLoaded = false

    private var _itemMenus = arrayListOf<XmbMenuItem>()

    override val isIconLoaded: Boolean
        get() = _isIconLoaded

    override val hasIcon: Boolean
        get() = _hasIcon
    override val icon: Bitmap
        get() = _icon!!

    override val hasMenu: Boolean
        get() = true
    override val menuItems: ArrayList<XmbMenuItem>?
        get() = _itemMenus
    override val menuItemCount: Int
        get() = _itemMenus.size

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

    init {
        vsh.M.media.createMediaMenuItems(_itemMenus, data)
    }

    private fun launch(i:XmbItem){
        vsh.openFileOnExternalApp(File(data.data))
    }

    override val onLaunch: (XmbItem) -> Unit
        get() = ::launch

    override val onScreenVisible: (XmbItem) -> Unit
        get() = ::loadIcon

    override val onScreenInvisible: (XmbItem) -> Unit
        get() = ::unloadIcon
}