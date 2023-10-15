package id.psw.vshlauncher.types

import android.os.Parcel
import android.os.Parcelable

class ExternalXmbItemHandle() : Parcelable {

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

    companion object CREATOR : Parcelable.Creator<ExternalXmbItemHandle> {
        override fun createFromParcel(parcel: Parcel): ExternalXmbItemHandle {
            return ExternalXmbItemHandle(parcel)
        }

        override fun newArray(size: Int): Array<ExternalXmbItemHandle?> {
            return arrayOfNulls(size)
        }
    }
}