package id.psw.vshlauncher.types.items

import android.graphics.Bitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.R
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.activities.XMB
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.types.XMBItem

class XMBItemCategory(
    private val vsh: VSH, private val cateId:String,
    private val strId : Int, private val iconId: Int,
    val sortable: Boolean = false, defaultSortIndex : Int
    ) : XMBItem(vsh) {
    private val _content = ArrayList<XMBItem>()
    private fun _postNoLaunchNotification(xmb: XMBItem){
        vsh.postNotification(null, vsh.getString(R.string.error_common_header), vsh.getString(R.string.error_category_launch))
    }

    private var _isLoadingIcon = false
    private lateinit var _icon : Bitmap
    override val isIconLoaded: Boolean get() = _isLoadingIcon
    override val hasBackSound: Boolean = false
    override val hasBackdrop: Boolean = false
    override val hasContent: Boolean = true
    override val hasIcon: Boolean = true
    override val hasAnimatedIcon: Boolean = false
    override val hasDescription: Boolean = false
    override val hasMenu = false
    override val isHidden: Boolean
        get() = vsh.isCategoryHidden(id)

    override val displayName: String get() = vsh.getString(strId)
    override val icon: Bitmap get() = _icon
    override val id: String get() = cateId
    private var _sortIndex = 0

    private val pkSortIndex : String get() ="${Consts.CATEGORY_SORT_INDEX_PREFIX}_${cateId}"

    var sortIndex : Int
        get() = _sortIndex
        set(value) {
            _sortIndex = value
            vsh.pref.edit().putInt(pkSortIndex, _sortIndex).apply()
        }

    init {
        _sortIndex = vsh.pref.getInt(pkSortIndex, defaultSortIndex)
        vsh.threadPool.execute {
            _isLoadingIcon = true
            _icon =
                ResourcesCompat.getDrawable(vsh.resources, iconId, null)?.toBitmap(300,300)
                    ?: TRANSPARENT_BITMAP
            _isLoadingIcon = false
        }
    }

    override val content: ArrayList<XMBItem> get() = _content

    fun addItem(item: XMBItem) {
        if(_content.indexOfFirst { it.id == item.id } == -1){
            _content.add(item)
        }
    }

    var onSetSortFunc : (XMBItemCategory, Any) -> Unit = { _, _sortMode -> }
    var onSwitchSortFunc : (XMBItemCategory) -> Unit = { }
    var getSortModeNameFunc : (XMBItemCategory) -> String = { "" }

    fun onSwitchSort() = onSwitchSortFunc(this)
    fun <T> setSort(sort:T) {
        synchronized(this){
            onSetSortFunc(this, sort as Any)
        }
    }
    val sortModeName : String get() = getSortModeNameFunc(this)

    override val onLaunch: (XMBItem) -> Unit get() = ::_postNoLaunchNotification
}