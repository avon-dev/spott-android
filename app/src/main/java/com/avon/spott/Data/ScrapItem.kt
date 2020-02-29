package com.avon.spott.Data

import android.os.Parcel
import android.os.Parcelable

data class ScrapItem(val posts_image: String, val back_image:String?=null, val id:Int) : Parcelable {

    constructor(parcel: Parcel) : this (
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {}

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(posts_image)
        parcel.writeString(back_image)
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScrapItem> {
        override fun createFromParcel(parcel: Parcel): ScrapItem {
            return ScrapItem(parcel)
        }

        override fun newArray(size: Int): Array<ScrapItem?> {
            return arrayOfNulls(size)
        }
    }
}