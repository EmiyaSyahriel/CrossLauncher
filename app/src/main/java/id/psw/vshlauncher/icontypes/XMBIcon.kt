package id.psw.vshlauncher.icontypes

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.customtypes.Icon
import id.psw.vshlauncher.move
import id.psw.vshlauncher.removeIfTrue
import id.psw.vshlauncher.views.VshView

open class XMBIcon(val itemId: String) {

    companion object{
        val TransparentDrawable = ColorDrawable(Color.TRANSPARENT)
        val TransparentBitmap = TransparentDrawable.toBitmap(1,1)
        val blank = Icon(TransparentBitmap, 75)
    }

    protected open val idPrefix = ""

    /// region Metadata
    open val id : String get() = idPrefix + itemId
    open val name : String = ""
    open val description : String = ""
    open val hasDescription : Boolean = false
    open var icon : Icon = blank
    open val isVisible : Boolean get() = true
    open var selectedIndex : Int = 0
    /// endregion

    /// region Contents
    val content = ArrayList<XMBIcon>()
    open val hasContent : Boolean
        get() = content.isNotEmpty()
    open fun getContent(index:Int) : XMBIcon = content[index]
    open fun getContent():XMBIcon = content[contentIndex]
    open fun getContent(vararg indices : Int) : XMBIcon{
        var retval = this
        for(i in indices){
            if(retval.hasContent){
                retval = retval.getContent(i)
            }
        }
        return retval
    }
    open fun addContent(item:XMBIcon) = content.add(item)
    open fun delContent(item:XMBIcon) = content.add(item)
    open fun delContent(ID:String) = content.removeIfTrue { it.id.equals( ID, true) }
    open fun findContent(ID:String) : XMBIcon? = content.find { it.id.equals( ID, true) }
    open fun moveContent(from:Int, to:Int) = content.move(from, to)
    open fun forEachContentIndexed (func : (Int, XMBIcon) -> Unit) = content.forEachIndexed(func)
    open var contentIndex : Int = 0
    open val contentSize : Int get() = content.size
    /// endregion

    /// region Menu
    val menu = ArrayList<XMBMenuEntry>()
    open val hasMenu : Boolean get() = menu.isNotEmpty()
    open fun getMenu(index:Int): XMBMenuEntry = menu[index]
    open fun getMenu():XMBMenuEntry = menu[menuIndex]
    open fun addMenu(menuEntry:XMBMenuEntry) = menu.add(menuEntry)
    open fun delMenu(menuEntry:XMBMenuEntry) = menu.remove(menuEntry)
    open fun delMenu(ID:String)  = menu.removeIfTrue { it.id.equals(ID, true) }
    open fun findMenu(ID:String) : XMBMenuEntry? = menu.find { it.id.equals( ID, true) }
    open fun findMenu(menuIndex:Int) : XMBMenuEntry? = menu.find { it.id.equals( "menu_${id}_${menuIndex}", true) }
    open fun moveMenu(from:Int, to:Int) = menu.move(from, to)
    open fun createMenu() : MenuEntryBuilder = MenuEntryBuilder(this, "menu_$id")
    open fun forEachMenuIndexed (func : (Int, XMBMenuEntry) -> Unit) = menu.forEachIndexed(func)
    open val menuCount : Int get()= menu.size
    open var menuIndex : Int = 0
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