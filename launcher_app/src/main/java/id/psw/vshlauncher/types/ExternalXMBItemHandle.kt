package id.psw.vshlauncher.types

import android.os.Parcel
import android.os.Parcelable

class ExternalXMBItemHandle() : Parcelable {

    var id:Int = 0
    var parentId : Int = 0
    var packageName:String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        parentId = parcel.readInt()
        packageName = parcel.readString() ?: ""
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(parentId)
        parcel.writeString(packageName)
    }

    companion object CREATOR : Parcelable.Creator<ExternalXMBItemHandle> {
        override fun createFromParcel(parcel: Parcel): ExternalXMBItemHandle {
            return ExternalXMBItemHandle(parcel)
        }

        override fun newArray(size: Int): Array<ExternalXMBItemHandle?> {
            return arrayOfNulls(size)
        }
    }
}