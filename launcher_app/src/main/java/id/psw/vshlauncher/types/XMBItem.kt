package id.psw.vshlauncher.types

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.core.graphics.drawable.toBitmap
import id.psw.vshlauncher.Consts
import id.psw.vshlauncher.VSH
import id.psw.vshlauncher.select
import id.psw.vshlauncher.types.sequentialimages.XMBAnimBitmap
import id.psw.vshlauncher.types.sequentialimages.XMBFrameAnimation

open class XMBItem(private val vsh: VSH) {
    companion object {
        val WHITE_BITMAP : Bitmap = ColorDrawable(Color.WHITE).toBitmap(1,1)
        val TRANSPARENT_BITMAP : Bitmap = ColorDrawable(Color.TRANSPARENT).toBitmap(1,1)
        val WHITE_ANIM_BITMAP : XMBFrameAnimation = XMBAnimBitmap(1.0f, WHITE_BITMAP)
        val TRANSPARENT_ANIM_BITMAP : XMBFrameAnimation = XMBAnimBitmap(1.0f, TRANSPARENT_BITMAP)
        val SILENT_AUDIO : AudioTrack = AudioTrack(
            AudioManager.STREAM_SYSTEM,
            44100,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_8BIT,
            AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT),
            AudioTrack.MODE_STATIC,
            90
        )
        val EmptyLaunchImpl : (XMBItem) -> Unit = { }
    }

    // Metadata
    open val hasDescription : Boolean get() = false
    open val hasIcon : Boolean get() = false
    open val hasAnimatedIcon : Boolean get() = false
    open val hasBackdrop : Boolean get() = false
    open val hasBackSound : Boolean get() = false
    open val hasMenu : Boolean get() = (menuItems?.size ?: 0) > 0
    open val hasContent : Boolean get() = (content?.size ?: 0) > 0

    open val isIconLoaded : Boolean get() = false
    open val isAnimatedIconLoaded : Boolean get() = false
    open val isBackdropLoaded : Boolean get() = false
    open val isBackSoundLoaded : Boolean get() = false
    open val isHidden : Boolean get() = false

    open val id : String get() = Consts.XMB_DEFAULT_ITEM_ID

    // Displayed Item
    open val description : String get() = Consts.XMB_DEFAULT_ITEM_DESCRIPTION
    open val displayName : String get() = Consts.XMB_DEFAULT_ITEM_DISPLAY_NAME
    open val icon : Bitmap get() = XMBItem.TRANSPARENT_BITMAP
    open val animatedIcon : XMBFrameAnimation get() = XMBItem.TRANSPARENT_ANIM_BITMAP
    open val backdrop : Bitmap get() = XMBItem.TRANSPARENT_BITMAP
    open val animatedBackdrop : XMBFrameAnimation get() = XMBItem.TRANSPARENT_ANIM_BITMAP
    open val backSound : AudioTrack get() = SILENT_AUDIO

    open val menuItems : ArrayList<XMBMenuItem>? = null
    open val content : ArrayList<XMBItem>? = null

    open val onLaunch : (XMBItem) -> Unit = EmptyLaunchImpl
    open val onScreenVisible : (XMBItem) -> Unit = EmptyLaunchImpl
    open val onScreenInvisible : (XMBItem) -> Unit = EmptyLaunchImpl

    open val menuItemCount get() = menuItems?.size ?: 0
    open val contentCount get() = content?.size ?: 0

    fun launch(){
        onLaunch(this)
    }

    private var lastScreenVisibility = false
    var lastSelectedItemId = ""

    var screenVisibility : Boolean
    get() = lastScreenVisibility
    set(v) {
        if(v != lastScreenVisibility){
            v.select(onScreenVisible, onScreenInvisible)(this)
            lastScreenVisibility = v
        }
    }

    override fun toString(): String = "${displayName} - [${id}]"
}