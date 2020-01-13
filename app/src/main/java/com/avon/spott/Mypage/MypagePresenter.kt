package com.avon.spott.Mypage

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

    override fun openPhoto() {
        mypageView.showPhotoUi()
    }

    override fun clickAddPhoto(){
        if (!mypageView.checkPermission()) {
            mypageView.showPermissionDialog()
            return
        }
        mypageView.openGallery()

    }
}