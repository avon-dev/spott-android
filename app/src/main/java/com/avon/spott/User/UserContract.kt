package com.avon.spott.User

import com.avon.spott.BaseView
import com.avon.spott.Data.MapCluster
import com.google.android.gms.maps.model.LatLng

interface UserContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi(id:Int)
        fun setUserInfo(nickname:String, photo:String?)
        fun noPhoto()
        fun addItems(userItems:ArrayList<MapCluster>)
//        fun movePosition(latLng: LatLng, zoom: Float)
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun getUserphotos(baseurl:String, userId:Int)

    }
}