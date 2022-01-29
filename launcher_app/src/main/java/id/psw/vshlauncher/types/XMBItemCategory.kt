package id.psw.vshlauncher.types

import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.postNotification
import java.util.concurrent.ThreadPoolExecutor

class XMBItemCategory(private val vsh: VSH, private val strId : Int, private val iconId: Int) : XMBItem(vsh) {
    private val _content = ArrayList<XMBItem>()
    private fun _postNoLaunchNotification(xmb:XMBItem){
        vsh.postNotification(null, vsh.getString(R.string.error_common_header), vsh.getString(R.string.error_category_launch))
    }

    private var _isLoadingIcon = false
    private lateinit var _icon : Bitmap
    override val isLoadingIcon: Boolean get() = _isLoadingIcon
    override val hasAnimatedBackdrop: Boolean = false
    override val hasBackSound: Boolean = false
    override val hasBackdrop: Boolean = false
    override val hasContent: Boolean = true
    override val hasIcon: Boolean = true
    override val hasAnimatedIcon: Boolean = false
    override val hasDescription: Boolean = false
    override val hasMenu = false

    override val displayName: String get() = vsh.getString(strId)
    override val icon: Bitmap get() = _icon

    init {
        vsh.threadPool.execute {
            _isLoadingIcon = true
            _icon =
                ResourcesCompat.getDrawable(vsh.resources, iconId, null)?.toBitmap(300,300)
                    ?: TRANSPARENT_BITMAP
            _isLoadingIcon = false
        }
    }

    override val content: ArrayList<XMBItem> get() = _content

    fun addItem(item:XMBItem) {

    }

    override val onLaunch: (XMBItem) -> Unit get() = ::_postNoLaunchNotification
}