package com.avon.spott.User

class UserPresenter (val userView:UserContract.View):UserContract.Presenter {

    init{userView.presenter = this}

    override fun openPhoto() {
        userView.showPhotoUi()
    }
}