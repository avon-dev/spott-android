package com.avon.spott.Map

import com.avon.spott.Data.CameraRange
import com.avon.spott.Data.Map
import com.avon.spott.Data.Number
import com.avon.spott.Data.User
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

    val adddummy = addDummy() //테스트 코드 추가

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

    override fun getPhotos(baseUrl:String, latLngBounds: LatLngBounds) {

        val cameraRange = CameraRange(latLngBounds.northeast.latitude, latLngBounds.northeast.longitude,
            latLngBounds.southwest.latitude, latLngBounds.southwest.longitude)

       //------------------------200115 테스트 코드-------------------------------------------
//        val newArryList = ArrayList<Map>()
//        for(i in 0..adddummy.size-1){
//            if(adddummy[i].latitude < latLngBounds.northeast.latitude &&
//                adddummy[i].latitude >  latLngBounds.southwest.latitude &&
//                adddummy[i].longitude < latLngBounds.northeast.longitude &&
//                adddummy[i].longitude > latLngBounds.southwest.longitude  ){
//                newArryList.add(adddummy[i])
//            }
//
//        }
//        mapView.addItems(newArryList)
        //-----------------------------------------------------------------------------------


        //------------------- json parser테스트------------------------------------------------
//        var parser = Parser.toJson(addDummy())
//        parser = "{ \"payload\" : " + parser + "}"
//        logd(TAG, "1. 투제이슨 ===== " +parser)
//        val presult = Parser.fromJson<ArrayList<Map>>(parser)
//        logd(TAG, "2. 프롬제이슨 ===== " +presult)
        //----------------------------------------------------------------------------------

        Retrofit(baseUrl).get("/spott/posts",  Parser.toJson(cameraRange))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                logd(TAG,"스트링 테스트 : " + string)
                var changeString = string!!.replace("'", "")
                val parserString = "{ \"payload\" : " + changeString + "}"
                 val photos = Parser.fromJson<ArrayList<Map>>(parserString)

                logd(TAG,"리절트는 " +photos)

                if(photos!=null){
                    mapView.addItems(photos)
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

//         mapView.addItems(adddummy) /** 더미 넣는 테스트 코드 **/
    }

    override fun getNophoto(){ //테스트용
        mapView.addItems(ArrayList<Map>())
        mapView.noPhoto()
    }

    /*============================= 더미 데이터 넣는 코드=========================================== */
    private fun addDummy() : ArrayList<Map>{

        var mapItems = ArrayList<Map>()
        mapItems.add(Map(37.565597, 126.978009,"https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg",0))
        mapItems.add(Map(37.564920, 126.925100,"https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg",0))
        mapItems.add(Map(37.547759, 126.922873,"https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg",0))
        mapItems.add(Map(37.504458, 126.986861,"https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg",0))

        for(i in 0..102){
            val position = position()
            mapItems.add(Map(position.latitude, position.longitude, "https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg",1))
        }

        return mapItems
    }
    /*============================= 더미 데이터 넣는 코드 끝======================================= */

    /*==========================구글맵 더미 데이터용 랜덤위치 생성 함수들==================================*/
    private fun position():LatLng{
        return LatLng(random(37.489324, 37.626495), random(126.903712, 127.096659))
    }

    private fun random(min:Double, max:Double):Double{
        return min+(max-min)* Random().nextDouble()
    }
    /*=========================구글맵 더미 데이터용 랜덤위치 생성 함수들 끝=================================*/
}