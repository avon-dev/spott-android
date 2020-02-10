package com.avon.spott.Photomap

import com.google.android.gms.maps.model.LatLng

class PhotoMapPresenter(val photoMapView:PhotoMapContract.View) : PhotoMapContract.Presenter {
    init{ photoMapView.presenter = this}

    override fun moveToSpot(latLng: LatLng){
        photoMapView.animCamera(latLng)
    }

    override fun getMylocation() {
        if(!photoMapView.checkPermission()){
            photoMapView.showPermissionDialog()
            return
        }
        if(photoMapView.mylocation !=null){
            photoMapView.showMylocation()
            photoMapView.animCamera(photoMapView.mylocation!!)
        }else{
            photoMapView.showProgressbar(true)
            photoMapView.startLocationUpdates()
        }
    }
}