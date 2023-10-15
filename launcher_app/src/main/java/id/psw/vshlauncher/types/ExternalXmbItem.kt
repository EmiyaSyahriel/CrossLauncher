package id.psw.vshlauncher.types

import android.os.Parcel
import android.os.Parcelable
import id.psw.vshlauncher.readByteBoolean
import id.psw.vshlauncher.writeByteBoolean

class ExternalXmbItem() : Parcelable {
    companion object CREATOR : Parcelable.Creator<ExternalXmbItem> {
        const val PSW_XMB_HEADER = "[:psw:]"
        const val PSW_XMB_META_SEPARATOR = "[:meta:]"
        const val PSW_XMB_MEDIA_SEPARATOR = "[:media:]"

        override fun createFromParcel(parcel: Parcel): ExternalXmbItem {
            return ExternalXmbItem(parcel)
        }

        override fun newArray(size: Int): Array<ExternalXmbItem?> {
            return arrayOfNulls(size)
        }
    }

    var packageId : String = ""
    var baseId : Int = 0
    var parentId : Int = 0

    var displayName : String = ""
    var description : String = ""

    var hasIcon = false
    var hasMenu = false
    var iconUrl : String= ""
    var hasAnimatedIcon = false
    var animatedIconUrl : String = ""
    var hasBackdrop = false
    var hasAnimatedBackdrop = false
    var backdropUrl : String = ""
    var hasBackSound = false
    var backSoundUrl : String = ""

    var playBootAnimation : Boolean = false

    constructor(parcel: Parcel) : this() {
        if(parcel.readString() != PSW_XMB_HEADER){
            throw Exception("Invalid Parcel Initial Header")
        }
        packageId = parcel.readString() ?: ""
        baseId = parcel.readInt()
        parentId = parcel.readInt()

        if(parcel.readString() != PSW_XMB_META_SEPARATOR){
            throw Exception("Invalid Metadata Separator")
        }

        displayName = parcel.readString() ?: ""
        description = parcel.readString() ?: ""

        if(parcel.readString() != PSW_XMB_MEDIA_SEPARATOR){
            throw Exception("Invalid Media Data Separator")
        }

        hasIcon = parcel.readByteBoolean()
        hasAnimatedIcon = parcel.readByteBoolean()
        hasBackdrop = parcel.readByteBoolean()
        hasAnimatedBackdrop = parcel.readByteBoolean()
        hasBackSound = parcel.readByteBoolean()
        hasMenu = parcel.readByteBoolean()

        iconUrl = parcel.readString() ?: ""
        animatedIconUrl = parcel.readString() ?: ""
        backdropUrl = parcel.readString() ?: ""
        backSoundUrl = parcel.readString() ?: ""
        playBootAnimation = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(PSW_XMB_HEADER)
        parcel.writeString(packageId)
        parcel.writeInt(baseId)
        parcel.writeInt(parentId)
        parcel.writeString(PSW_XMB_META_SEPARATOR)
        parcel.writeString(displayName)
        parcel.writeString(description)
        parcel.writeString(PSW_XMB_MEDIA_SEPARATOR)

        parcel.writeByteBoolean(hasIcon)
        parcel.writeByteBoolean(hasAnimatedIcon)
        parcel.writeByteBoolean(hasBackdrop)
        parcel.writeByteBoolean(hasAnimatedBackdrop)
        parcel.writeByteBoolean(hasBackSound)
        parcel.writeByteBoolean(hasMenu)

        if(hasIcon) parcel.writeString(iconUrl)
        if(hasAnimatedIcon) parcel.writeString(animatedIconUrl)
        if(hasBackdrop || hasAnimatedBackdrop) parcel.writeString(backdropUrl)
        if(hasBackSound) parcel.writeString(backSoundUrl)
        parcel.writeByteBoolean(playBootAnimation)
    }

    override fun describeContents(): Int {
        return 0
    }

}