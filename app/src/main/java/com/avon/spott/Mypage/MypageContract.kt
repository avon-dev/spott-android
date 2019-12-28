package com.avon.spott.Mypage

import com.avon.spott.BaseView

interface MypageContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
        fun showAlarmUi()
        fun showEditMyInfoUi()
        fun showAddPhotoUi()
    }

    interface Presenter{
        fun openPhoto()
        fun openAlarm()
        fun openEditMyInfo()
        fun openAddPhoto()
    }

}