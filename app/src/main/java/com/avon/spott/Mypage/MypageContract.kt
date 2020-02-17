package com.avon.spott.Mypage

import com.avon.spott.BaseView
import com.avon.spott.Data.MapCluster
import com.google.android.gms.maps.model.LatLng

interface MypageContract {

    interface View: BaseView<Presenter> {
        fun addItems(mypageItems:ArrayList<MapCluster>)
        fun showPhotoUi(id:Int)
        fun showAlarmUi()
        fun showEditMyInfoUi()
        fun showAddPhotoUi(mFilePath : String)
        fun checkPermission():Boolean
        fun showPermissionDialog()
        fun openGallery()
        fun noPhoto()
        fun movePosition(latLng: LatLng, zoom: Float)
        fun setUserInfo(nickname:String, photo:String?)
        fun clearAdapter()
    }

    interface Presenter{
        fun getMyphotos(baseUrl:String)
        fun openPhoto(id:Int)
        fun openAlarm()
        fun openEditMyInfo()
        fun openAddPhoto(mFilePath : String)
        fun clickAddPhoto()
    }

}