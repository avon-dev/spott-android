package com.avon.spott.Map

import com.avon.spott.BaseView
import com.avon.spott.Data.Map
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

interface MapContract {
    interface View: BaseView<Presenter> {
        fun showPhotoUi(id:Int)
        fun movePosition(latLng: LatLng, zoom:Float)
        fun showToast(string: String)
        fun sendCameraRange()
        fun noPhoto()

        fun addItems(mapItems:ArrayList<Map>)
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun getLastPosition()
        fun setLastPosition(cameraPosition: CameraPosition)
        fun getPhotos(baseUrl:String, latLngBounds: LatLngBounds)

        fun getNophoto() //테스트용
    }
}