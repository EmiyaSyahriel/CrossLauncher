package id.psw.vshlauncher.types

import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import id.psw.vshlauncher.R
import id.psw.vshlauncher.Vsh
import id.psw.vshlauncher.VshResTypes
import id.psw.vshlauncher.delayedExistenceCheck
import id.psw.vshlauncher.getOrMake
import id.psw.vshlauncher.postNotification
import id.psw.vshlauncher.submodules.BitmapRef
import id.psw.vshlauncher.types.sequentialimages.XmbAnimApng
import id.psw.vshlauncher.types.sequentialimages.XmbAnimGif
import id.psw.vshlauncher.types.sequentialimages.XmbAnimMmr
import id.psw.vshlauncher.types.sequentialimages.XmbAnimWebP
import id.psw.vshlauncher.types.sequentialimages.XmbFrameAnimation
import id.psw.vshlauncher.uniqueActivityName
import java.io.File

/**
 * Content Information Files structure loader
 *
 * Since XmbAppItem and XmbShortcutItem is generic, this class is to make sure
 * the loader implementation have the same exact behaviour
 */
class CifLoader {
    companion object {
        var videoIconMode = VideoIconMode.AllTime
        var disableBackSound = false
        var disableBackdrop = false
        var disableBackdropOverlay = false
        const val DEFAULT_BITMAP_REF = "None"
        val default_bitmap = BitmapRef("none", {XmbItem.TRANSPARENT_BITMAP}, BitmapRef.FallbackColor.Transparent)
        private val ios = mutableMapOf<File, Ref<Boolean>>()
        private val ioc = mutableMapOf<File, Ref<Int>>()
    }

    private val _iconSync = Object()
    private val _animIconSync = Object()
    private val _backdropSync = Object()
    private val _portBackdropSync = Object()
    private val _backSoundSync = Object()
    private lateinit var vsh : Vsh
    private var root = ArrayList<File>()
    private var resInfo : ResolveInfo? = null
    private var itemId = ""

    constructor(vsh : Vsh, resInfo : ResolveInfo, root : ArrayList<File>){
        this.vsh = vsh
        this.root.addAll(root)
        this.resInfo = resInfo
        itemId = resInfo.uniqueActivityName

        listCustomFiles()
    }

    constructor(vsh : Vsh, id:String, directory : File){
        this.vsh = vsh
        itemId = id
        root.add(directory)

        listCustomFiles()
    }

    private var _animIcon : XmbFrameAnimation = XmbItem.TRANSPARENT_ANIM_BITMAP
    private var _backdrop = default_bitmap
    private var _backOverlay = default_bitmap
    private var _portBackdrop = default_bitmap
    private var _portBackOverlay = default_bitmap
    private var _backSound : File = XmbItem.SILENT_AUDIO
    private var _icon = default_bitmap

    val icon get() = _icon
    val backdrop get()= _backdrop
    val portBackdrop get() = _portBackdrop
    val backSound get() = _backSound
    val backOverlay get() = _backOverlay
    val portBackOverlay get() = _portBackOverlay
    val animIcon get() = _animIcon

    fun requestCustomizationFiles(fileBaseName : String, extensions: Array<String>) : ArrayList<File>{
        return ArrayList<File>().apply {
            for(s in root){
                for(e in extensions){
                    add(File(s, "$fileBaseName.$e"))
                }
            }
        }
    }

    private fun createCustomizationFileArray(isDisabled: Boolean, fileBaseName: String, extensions : Array<String>)
    : ArrayList<File>
    {
        return ArrayList<File>().apply {
            if(!isDisabled) addAll(requestCustomizationFiles(fileBaseName, extensions))
        }
    }

    private fun listCustomFiles(){
        backdropFiles = createCustomizationFileArray(disableBackdrop,"PIC1",VshResTypes.IMAGES)
        backdropOverlayFiles = createCustomizationFileArray(disableBackdropOverlay,"PIC0",VshResTypes.IMAGES)
        portraitBackdropFiles = createCustomizationFileArray(disableBackdrop,"PIC1_P",VshResTypes.IMAGES)
        portraitBackdropOverlayFiles = createCustomizationFileArray(disableBackdropOverlay,"PIC0_P",VshResTypes.IMAGES)
        animatedIconFiles = createCustomizationFileArray(videoIconMode == VideoIconMode.Disabled,"ICON1",VshResTypes.ANIMATED_ICONS)
        iconFiles = createCustomizationFileArray(false,"ICON0",VshResTypes.ICONS)
        backSoundFiles = createCustomizationFileArray(disableBackSound,"SND0",VshResTypes.SOUNDS)
    }

    private lateinit var backdropFiles : ArrayList<File>
    private lateinit var backdropOverlayFiles : ArrayList<File>
    private lateinit var portraitBackdropFiles : ArrayList<File>
    private lateinit var portraitBackdropOverlayFiles : ArrayList<File>
    private lateinit var animatedIconFiles : ArrayList<File>
    private lateinit var iconFiles : ArrayList<File>
    private lateinit var backSoundFiles : ArrayList<File>
    private var _hasAnimIconLoaded = false
    val hasIconLoaded get() = _icon.isLoaded
    val hasAnimIconLoaded get() = _hasAnimIconLoaded
    val hasBackSoundLoaded get() = _backSound.exists()
    val hasBackdropLoaded get() = _backdrop.isLoaded
    val hasPortBackdropLoaded get() = _portBackdrop.isLoaded
    private fun <K> MutableMap<File, Ref<K>>.getOrMake(k:File, refDefVal:K) = getOrMake<File, Ref<K>>(k){ Ref<K>(refDefVal) }
    private fun ArrayList<File>.checkAnyExists() : Boolean = any { it.delayedExistenceCheck(ioc.getOrMake(it, 0), ios.getOrMake(it, false)) }
    val hasBackdrop : Boolean get() =                   !disableBackdrop && backdropFiles.checkAnyExists()
    val hasPortraitBackdrop : Boolean get() =           !disableBackdrop && backdropOverlayFiles.checkAnyExists()
    val hasBackOverlay : Boolean get() =                !disableBackdropOverlay && backdropOverlayFiles.checkAnyExists()
    val hasPortraitBackdropOverlay : Boolean get() =    !disableBackdropOverlay && portraitBackdropOverlayFiles.checkAnyExists()
    val hasBackSound : Boolean get() =                  !disableBackSound && backSoundFiles.checkAnyExists()
    val hasAnimatedIcon : Boolean get() =               videoIconMode != VideoIconMode.Disabled && animatedIconFiles.checkAnyExists()


    fun loadIcon(){
        // No need to sync, BitmapRef loaded directly
        _icon = BitmapRef("${itemId}_icon", {
            var found = false
            var rv : Bitmap? = null
            val file = iconFiles.firstOrNull {it.exists()}

            if(file != null){
                try{
                    rv = BitmapFactory.decodeFile(file.absolutePath)
                    found = true
                }catch(e:Exception){
                    vsh.postNotification(
                        null,
                        vsh.getString(R.string.error_common_header),
                        "Icon file for package $itemId is corrupted : $file :\n${e.message}"
                    )
                }
            }

            if(!found ){
                val ri = resInfo
                if(ri != null){
                    rv = vsh.M.icons.create(ri.activityInfo, vsh)
                }
            }
            rv
        })

        if(vsh.playAnimatedIcon){
            synchronized(_animIconSync){
                if((!_hasAnimIconLoaded || _animIcon.hasRecycled)){
                    for(file in animatedIconFiles){
                        if(file.exists() || file.isFile){
                            _animIcon = when (file.extension.uppercase()) {
                                "WEBP" -> XmbAnimWebP(file)
                                "APNG" -> XmbAnimApng(file)
                                "MP4" -> XmbAnimMmr(file.absolutePath)
                                "GIF" -> XmbAnimGif(file)
                                else -> XmbItem.WHITE_ANIM_BITMAP
                            }
                            _hasAnimIconLoaded = true
                            break
                        }
                    }
                }
            }
        }
    }

    fun unloadIcon(){

        if(_icon.id != default_bitmap.id) _icon.release()

        synchronized(_animIconSync){
            if(_hasAnimIconLoaded && !_animIcon.hasRecycled){
                _hasAnimIconLoaded = false
                if(_animIcon != XmbItem.WHITE_ANIM_BITMAP && _animIcon != XmbItem.TRANSPARENT_ANIM_BITMAP) _animIcon.recycle()
                _animIcon = XmbItem.TRANSPARENT_ANIM_BITMAP
            }
        }
    }

    fun loadBackdrop(){
        _backdrop = BitmapRef("${itemId}_backdrop", {
            val f = backdropFiles.find {
                it.exists()
            }
            if(f != null) BitmapFactory.decodeFile(f.absolutePath)else null
        })
    }

    fun unloadBackdrop(){
        if(_backdrop.id != default_bitmap.id) _backdrop.release()
        _backdrop = default_bitmap
    }

    fun loadSound(){
        backSoundFiles.find { it.exists() }?.let {
            _backSound = it
        }
    }

    fun unloadSound(){
        _backSound = XmbItem.SILENT_AUDIO
    }

}