package id.psw.vshlauncher.icontypes

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap

open class XMBIcon(val itemId: String) {

    companion object{
        val blank = ColorDrawable(Color.TRANSPARENT)
        val blankBmp = blank.toBitmap(1,1)
    }

    protected open val idPrefix = ""

    /// region Metadata
    open val id : String get() = idPrefix + itemId
    open val name : String = ""
    open val description : String = ""
    open val hasDescription : Boolean = false
    open var activeIcon : Bitmap = blankBmp
    open var inactiveIcon : Bitmap = blankBmp
    open val isVisible : Boolean get() = true
    open var selectedIndex : Int = 0
    /// endregion

    /// region Contents
    val content = ArrayList<XMBIcon>()
    open val hasContent : Boolean
        get() = content.isNotEmpty()
    var contentIndex : Int = 0
    val contentSize : Int get() = content.size
    /// endregion

    /// region Menu
    val menu = ArrayList<XMBMenuEntry>()
    open val hasMenu : Boolean get() = menu.isNotEmpty()
    val menuCount : Int get()= menu.size
    var menuIndex : Int = 0
    /// endregion

    /// region Callbacks

    /**
     * When the app icon isClicked
     */
    open fun onLaunch() { }
    open fun onVisibilityChange(isVisible: Boolean) { }
    open fun onSelectionChanged(isSelected: Boolean) { }

    var isCoordinatelyVisible : Boolean = false
        set(value){
            if(field != value){
                onVisibilityChange(value)
            }
            field = value
        }

    var isSelected : Boolean = false
        set(value){
            if(field != value){
                onSelectionChanged(value)
            }
            field = value
        }
    /// endregion

    override fun toString(): String = "[XMBIcon::${javaClass.name} #$id] $name"

    class MenuEntryBuilder(val icon:XMBIcon, val prefix:String) {
        private val entries : ArrayList<XMBMenuEntry> = ArrayList()
        fun add(name:String, onClick : () -> Unit) : MenuEntryBuilder{
            entries.add(XMBMenuEntry("${prefix}_${entries.size}").apply {

                this.name = name
                this.onClick = Runnable(onClick)
                this.selectable = true
            })
            return this
        }
        fun separator(){
            entries.add(XMBMenuEntry("${prefix}_${entries.size}").apply {
                this.onClick = Runnable {  }
                this.name = ""
                this.selectable = false
            }
            )
        }
        fun apply(clearContainer:Boolean = false) : ArrayList<XMBMenuEntry> {
            if(clearContainer) icon.menu.clear()
            entries.forEach { icon.menu.add(it) }
            return entries
        }
    }
}