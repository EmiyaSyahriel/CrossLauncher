package id.psw.crosslauncher.xlib

import android.content.Context
import android.graphics.Bitmap

abstract class XmbIconExtension(host: Context, self: Context) : XmbExtension(ExtensionType.Icon, host, self) {
    class BasicIconProperty<T>(override var content: T? = null) : Property<T>(){
        override val has : Boolean get(){
            val c = content
            if(c is String?) return !c.isNullOrBlank()
            return super.has
        }
    }

    open class ItemMenu {
        open val index = 0
        open val displayName = ""
        open fun launch() { }
    }

    open class Item {
        open val id = ""
        open val displayName = ""

        open val description : Property<String> = BasicIconProperty()

        open val icon : Property<Bitmap> = BasicIconProperty()
        open val animatedIcon : Property<Bitmap> = BasicIconProperty()
        open val backdrop : Property<Bitmap> = BasicIconProperty()
        open val backSound : Property<Bitmap> = BasicIconProperty()

        open val hasContent : Boolean = false
        open val hasMenu : Boolean = false

        /** Called when icon is launched */
        open fun onLaunched() {}
        /** Called when opening side menu, here you can update your menu content */
        open fun getMenu() : ArrayList<ItemMenu> = arrayListOf()
        /** Called when content is requested, here you can update your content */
        open fun getContent() : ArrayList<ItemMenu> = arrayListOf()
        /** Called when icon is getting in or getting out of screen */
        open fun onVisibilityChange(visible:Boolean){}
        /** Called when icon is getting selected or unselected */
        open fun onHoveringChange(hovered: Boolean){}
    }

    open class Category {
        open val icon : Bitmap? = null
        open val id = ""
        open val displayName = ""
        open fun onRequestItem() : ArrayList<Item> = arrayListOf()
    }

    open val categories = arrayListOf<Category>()
}