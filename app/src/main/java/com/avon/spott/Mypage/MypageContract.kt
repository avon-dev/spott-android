package com.avon.spott.Mypage

import com.avon.spott.BaseView
import com.avon.spott.Data.MapCluster

interface MypageContract {

    interface View: BaseView<Presenter> {
        fun addItmes(mypageItems:ArrayList<MapCluster>)
        fun showPhotoUi(id:Int)
        fun showAlarmUi()
        fun showEditMyInfoUi()
        fun showAddPhotoUi(mFilePath : String)
        fun checkPermission():Boolean
        fun showPermissionDialog()
        fun openGallery()
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