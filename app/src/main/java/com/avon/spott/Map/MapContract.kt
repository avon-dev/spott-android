package com.avon.spott.Map

import com.avon.spott.BaseView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

interface MapContract {
    interface View: BaseView<Presenter> {
        fun showPhotoUi()
        fun movePosition(latLng: LatLng, zoom:Float)
    }

    interface Presenter{
        fun openPhoto()
        fun getLastPosition()
        fun setLastPosition(cameraPosition: CameraPosition)
    }
}