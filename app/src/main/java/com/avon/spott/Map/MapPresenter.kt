package com.avon.spott.Map

import com.avon.spott.Data.*
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import retrofit2.HttpException
import java.util.*
import kotlin.collections.ArrayList

class MapPresenter (val mapView:MapContract.View) : MapContract.Presenter {

    private val TAG = "MapPresenter"

    init{ mapView.presenter = this}



    override fun openPhoto(id:Int) { mapView.showPhotoUi(id) }

    override fun getLastPosition() {
        if(App.prefs.mylastlat!=0.0f){
            mapView.movePosition(LatLng(App.prefs.mylastlat.toDouble(), App.prefs.mylastlng.toDouble()),
                App.prefs.mylastzoom)
        }else{  //앱을 설치 후 처음 실행하면 지도의 기본 위치는 서울시청.
            mapView.movePosition(LatLng(37.56668, 126.97843),14f)
        }
    }

    override fun setLastPosition(cameraPosition: CameraPosition) {
        // 마지막으로 봤던 위치, 줌을 SharedPreference에 저장
        App.prefs.mylastlat = cameraPosition.target.latitude.toFloat()
        App.prefs.mylastlng = cameraPosition.target.longitude.toFloat()
        App.prefs.mylastzoom = cameraPosition.zoom
    }

    override fun getMylocation() {
        if(!mapView.checkPermission()){
            mapView.showPermissionDialog()
            return
        }
        if(mapView.mylocation != null){
            mapView.showMylocation()
            mapView.moveToMylocation()
        }else{
            mapView.showProgressbar(true)
            mapView.startLocationUpdates()
        }
    }


    override fun getPhotos(baseUrl:String, latLngBounds: LatLngBounds, action:Int) {

        val cameraRange = CameraRange(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude,
            latLngBounds.southwest.latitude, latLngBounds.southwest.longitude, action)


        logd(TAG, "맵은"+ Parser.toJson(cameraRange))

        Retrofit(baseUrl).get(App.prefs.token,"/spott/map/posts",  Parser.toJson(cameraRange))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val photos = Parser.fromJson<ArrayList<MapCluster>>(string!!)

                 mapView.addItems(photos)

                if(photos.size==0){
                    mapView.noPhoto()
                }
            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
            })

    }

}