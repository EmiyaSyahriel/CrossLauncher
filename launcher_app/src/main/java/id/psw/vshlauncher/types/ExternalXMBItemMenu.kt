package id.psw.vshlauncher.types

import android.os.Parcel
import android.os.Parcelable
import id.psw.vshlauncher.readByteBoolean
import id.psw.vshlauncher.writeByteBoolean
import java.lang.Exception

class ExternalXMBItemMenu() : Parcelable {
    companion object CREATOR : Parcelable.Creator<ExternalXMBItemMenu> {
        const val XMB_MENU_HEADER = "[:menu:]"
        const val XMB_MENU_METADATA_SEPARATOR = "[:meta:]"
        override fun createFromParcel(parcel: Parcel): ExternalXMBItemMenu {
            return ExternalXMBItemMenu(parcel)
        }

        override fun newArray(size: Int): Array<ExternalXMBItemMenu?> {
            return arrayOfNulls(size)
        }
    }

    var baseId = 0
    var menuId = 0
    var packageId = ""

    var displayName = ""
    var isDisabled = false
    var isSeparator = false

    constructor(parcel: Parcel) : this() {
        if(parcel.readString() != XMB_MENU_HEADER) throw Exception("Invalid Header")
        baseId = parcel.readInt()
        menuId = parcel.readInt()
        packageId = parcel.readString() ?: ""
        if(parcel.readString() != XMB_MENU_METADATA_SEPARATOR) throw Exception("Invalid Metadata Separator")
        displayName = parcel.readString() ?: ""
        isDisabled = parcel.readByteBoolean()
        isSeparator = parcel.readByteBoolean()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(XMB_MENU_HEADER)
        parcel.writeInt(baseId)
        parcel.writeInt(menuId)
        parcel.writeString(packageId)
        parcel.writeString(XMB_MENU_METADATA_SEPARATOR)
        parcel.writeString(displayName)
        parcel.writeByteBoolean(isDisabled)
        parcel.writeByteBoolean(isSeparator)
    }
}