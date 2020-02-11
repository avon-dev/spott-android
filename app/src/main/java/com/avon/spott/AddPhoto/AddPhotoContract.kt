package com.avon.spott.AddPhoto

import android.net.Uri
import com.avon.spott.BaseView
import com.google.android.gms.maps.model.LatLng

interface AddPhotoContract {

    interface View: BaseView<Presenter> {
        fun showToast(string: String)
        fun getPath(uri: Uri): String
        fun navigateUp()
        fun setPhoto(photo:String)
        fun movePosition(latLng: LatLng, zoom:Float)
        fun addMarker(latLng: LatLng)
        fun focusEdit()
        fun showLoading(boolean: Boolean)
        fun enableTouching(boolean: Boolean)
    }

    interface Presenter{
        fun sendPhoto(baseUrl:String, photo: String, caption: String, latLng: LatLng?, public:Boolean)
        fun navigateUp()
        fun usePhoto(photo: String)
        fun newMarker(latLng: LatLng)
    }

}