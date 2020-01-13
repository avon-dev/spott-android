package com.avon.spott.Mypage

import com.avon.spott.BaseView

interface MypageContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
        fun showAlarmUi()
        fun showEditMyInfoUi()
        fun showAddPhotoUi(mFilePath : String)
        fun checkPermission():Boolean
        fun showPermissionDialog()
        fun openGallery()
    }

    interface Presenter{
        fun openPhoto()
        fun openAlarm()
        fun openEditMyInfo()
        fun openAddPhoto(mFilePath : String)
        fun clickAddPhoto()
    }

}