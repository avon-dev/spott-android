package com.avon.spott.Mypage

import com.avon.spott.Data.MapCluster
import com.avon.spott.Data.MypageResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.google.android.gms.maps.model.LatLng
import retrofit2.HttpException

class MypagePresenter(val mypageView:MypageContract.View):MypageContract.Presenter {

    private val TAG = "MypagePresenter"

    init{mypageView.presenter = this}

    override fun openAddPhoto(mFilePath : String) {
        mypageView.showAddPhotoUi(mFilePath)
    }

    override fun openAlarm() {
        mypageView.showAlarmUi()
    }

    override fun openEditMyInfo() {
        mypageView.showEditMyInfoUi()
    }

    override fun openPhoto(id:Int) {
        mypageView.showPhotoUi(id)
    }

    override fun clickAddPhoto(){
        if (!mypageView.checkPermission()) {
            mypageView.showPermissionDialog()
            return
        }
        mypageView.openGallery()
    }

    val adddummy = addDummy() //테스트 코드 추가

    override fun getMyphotos(baseUrl: String) {

        Retrofit(baseUrl).get(App.prefs.temporary_token, "/spott/mypage",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<MypageResult>(string!!)

                mypageView.setUserInfo(result.user.nickname, result.user.profile_image)

                if(result.posts.size==0){
                    mypageView.noPhoto()
                    mypageView.movePosition(LatLng(37.56668, 126.97843), 14f) //등록한 사진이 없으면 구글맵 카메라 서울시청으로 이동
                }else{
                    mypageView.movePosition(LatLng(result.posts[0].latitude, result.posts[0].longitude), 11f)
                    //등록한 사진이 있으면 가장 최신 등록된 사진의 위치로 이동
                }

                mypageView.addItems(result.posts)

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
            })


//        val photos = adddummy
//        if(photos.size==0){
//            mypageView.noPhoto()
//            mypageView.movePosition(LatLng(37.56668, 126.97843), 14f) //등록한 사진이 없으면 구글맵 카메라 서울시청으로 이동
//        }else{
//            mypageView.movePosition(LatLng(photos[0].latitude, photos[0].longitude), 11f)
//            //등록한 사진이 있으면 가장 최신 등록된 사진의 위치로 이동
//        }
//        mypageView.addItems(photos)

    }

    private fun addDummy() : ArrayList<MapCluster> {
        var mapItems = ArrayList<MapCluster>()
        for(i in 0..4) {
            mapItems.add(
                MapCluster(
                    37.565597 + (0.02*i),
                    126.978009 + (0.02*i),
                    "https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg",
                    0
                )
            )
            mapItems.add(
                MapCluster(
                    37.564920 + (0.02*i),
                    126.925100 + (0.02*i),
                    "https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg",
                    0
                )
            )
            mapItems.add(
                MapCluster(
                    37.547759+ (0.02*i),
                    126.922873 + (0.02*i),
                    "https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg",
                    0
                )
            )
            mapItems.add(
                MapCluster(
                    37.504458+ (0.02*i),
                    126.986861 + (0.02*i),
                    "https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg",
                    0
                )
            )
        }
        return mapItems
     }

}