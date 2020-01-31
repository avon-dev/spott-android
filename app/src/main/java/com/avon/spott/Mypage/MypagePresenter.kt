package com.avon.spott.Mypage

import com.avon.spott.Data.MapCluster

class MypagePresenter(val mypageView:MypageContract.View):MypageContract.Presenter {

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

        mypageView.addItmes(adddummy)

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