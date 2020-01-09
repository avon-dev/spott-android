package com.avon.spott.Map

import com.avon.spott.Utils.App
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class MapPresenter (val mapView:MapContract.View) : MapContract.Presenter {

    init{ mapView.presenter = this}

    override fun openPhoto() { mapView.showPhotoUi() }

    override fun getLastPosition() {
        if(App.prefs.mylastlat!=0.0f){
            mapView.movePosition(LatLng(App.prefs.mylastlat.toDouble(), App.prefs.mylastlng.toDouble()),
                App.prefs.mylastzoom)
        }else{  //앱을 설치 후 처음 실행하면 지도의 기본 위치는 서울.
            mapView.movePosition(LatLng(37.56668, 126.97843),14f)
        }
    }

    override fun setLastPosition(cameraPosition: CameraPosition) {
        App.prefs.mylastlat = cameraPosition.target.latitude.toFloat()
        App.prefs.mylastlng = cameraPosition.target.longitude.toFloat()
        App.prefs.mylastzoom = cameraPosition.zoom
    }
}