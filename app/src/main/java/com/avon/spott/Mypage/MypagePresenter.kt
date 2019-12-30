package com.avon.spott.Mypage

class MypagePresenter(val mypageView:MypageContract.View):MypageContract.Presenter {

    init{mypageView.presenter = this}

    override fun openAddPhoto() {
        mypageView.showAddPhotoUi()
    }

    override fun openAlarm() {
        mypageView.showAlarmUi()
    }

    override fun openEditMyInfo() {
        mypageView.showEditMyInfoUi()
    }

    override fun openPhoto() {
        mypageView.showPhotoUi()
    }
}