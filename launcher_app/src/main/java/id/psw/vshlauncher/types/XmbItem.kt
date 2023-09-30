package id.psw.vshlauncher.types

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.select
import id.psw.vshlauncher.types.items.XmbMenuItem
import id.psw.vshlauncher.types.sequentialimages.XmbAnimBitmap
import id.psw.vshlauncher.types.sequentialimages.XmbFrameAnimation
import java.io.File

open class XmbItem(private val vsh: Vsh) {
    companion object {
        val WHITE_BITMAP : Bitmap = ColorDrawable(Color.WHITE).toBitmap(1,1)
        val TRANSPARENT_BITMAP : Bitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
        val WHITE_ANIM_BITMAP : XmbFrameAnimation = XmbAnimBitmap(1.0f, WHITE_BITMAP)
        val TRANSPARENT_ANIM_BITMAP : XmbFrameAnimation = XmbAnimBitmap(1.0f, TRANSPARENT_BITMAP)
        lateinit var SILENT_AUDIO : File
        val EmptyLaunchImpl : (XmbItem) -> Unit = { }
    }

    // Metadata
    open val hasDescription : Boolean get() = false
    // ICON0.PNG
    open val hasIcon : Boolean get() = false
    // ICON0.PNG
    open val hasValue : Boolean get() = false
    // ICON1.PMF / ICON1.PAM
    open val hasAnimatedIcon : Boolean get() = false
    // PIC0.PNG
    open val hasBackOverlay : Boolean get() = false
    // PIC1.PNG
    open val hasBackdrop : Boolean get() = false
    // PIC1_P.PNG
    open val hasPortraitBackdrop : Boolean get() = false
    // PIC0_P.PNG
    open val hasPortraitBackdropOverlay : Boolean get() = false
    // SND0.AT3
    open val hasBackSound : Boolean get() = false
    open val hasMenu : Boolean get() = (menuItems?.size ?: 0) > 0
    open val hasContent : Boolean get() = (content?.size ?: 0) > 0

    open val isIconLoaded : Boolean get() = false
    open val isAnimatedIconLoaded : Boolean get() = false
    open val isBackdropLoaded : Boolean get() = false
    open val isBackdropOverlayLoaded : Boolean get() = false
    open val isPortraitBackdropLoaded : Boolean get() = false
    open val isPortraitBackdropOverlayLoaded : Boolean get() = false
    open val isBackSoundLoaded : Boolean get() = false
    open val isHidden : Boolean get() = false

    open val id : String get() = Consts.XMB_DEFAULT_ITEM_ID

    // Displayed Item
    open val description : String get() = Consts.XMB_DEFAULT_ITEM_DESCRIPTION
    open val displayName : String get() = Consts.XMB_DEFAULT_ITEM_DISPLAY_NAME
    open val value : String get() = Consts.XMB_DEFAULT_ITEM_VALUE
    open val icon : Bitmap get() = XmbItem.TRANSPARENT_BITMAP
    open val animatedIcon : XmbFrameAnimation get() = XmbItem.TRANSPARENT_ANIM_BITMAP
    open val backdrop : Bitmap get() = XmbItem.TRANSPARENT_BITMAP
    open val portraitBackdrop : Bitmap get() = XmbItem.TRANSPARENT_BITMAP
    open val backSound : File get() = SILENT_AUDIO

    open val menuItems : ArrayList<XmbMenuItem>? = null
    open val content : ArrayList<XmbItem>? = null

    open val onLaunch : (XmbItem) -> Unit = EmptyLaunchImpl
    open val onScreenVisible : (XmbItem) -> Unit = EmptyLaunchImpl
    open val onScreenInvisible : (XmbItem) -> Unit = EmptyLaunchImpl
    open val onHovered : (XmbItem) -> Unit = EmptyLaunchImpl
    open val onUnHovered : (XmbItem) -> Unit = EmptyLaunchImpl

    open val menuItemCount get() = menuItems?.size ?: 0
    open val contentCount get() = content?.size ?: 0

    fun launch(){
        onLaunch(this)
    }

    /**
     * Called when menu is tried to be opened
     *
     * On this opportunity, You can update the menu content
     * and returns on which index the selected menu is currently at
     */
    open val onSetMenuOpened : ((Boolean) -> Int) get() = { _ -> 0}

    fun setMenuOpened(open : Boolean) : Int{
        return onSetMenuOpened(open)
    }

    private var lastScreenVisibility = false
    private var lastIsHovered =false
    var lastSelectedItemId = ""

    var screenVisibility : Boolean
    get() = lastScreenVisibility
    set(v) {
        if(v != lastScreenVisibility){
            v.select(onScreenVisible, onScreenInvisible)(this)
            lastScreenVisibility = v
        }
    }
    var isHovered : Boolean
    get() = lastIsHovered
    set(v){
        if(v != lastIsHovered){
            v.select(onHovered, onUnHovered)(this)
            lastIsHovered = v
        }
    }

    protected val propertyData = mutableMapOf<String, Any>()

    fun <T> getProperty(name:String, defVal : T) : T{
        if(propertyData.containsKey(name)){
            val casted = propertyData[name]!! as T
            return casted ?: defVal
        }
        return defVal
    }

    fun <T> setProperty(name:String, value:T){
        propertyData[name] = value as Any
    }


    override fun toString(): String = "$displayName - [${id}]"
}