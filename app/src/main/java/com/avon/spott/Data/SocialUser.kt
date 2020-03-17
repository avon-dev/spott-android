package com.avon.spott.Data

import android.os.Parcel
import android.os.Parcelable

data class SocialUser(val email:String, val user_type:Int, var nickname:String?=null) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString()
    ) {}

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeInt(user_type)
        parcel.writeString(nickname)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SocialUser> {
        override fun createFromParcel(parcel: Parcel): SocialUser {
            return SocialUser(parcel)
        }

        override fun newArray(size: Int): Array<SocialUser?> {
            return arrayOfNulls(size)
        }
    }
}