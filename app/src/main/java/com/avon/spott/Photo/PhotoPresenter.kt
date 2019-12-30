package com.avon.spott.Photo

class PhotoPresenter (val photoView:PhotoContract.View) : PhotoContract.Presenter{
    init{ photoView.presenter = this}

    override fun openPhotoMap() {
        photoView.showPhotoMapUi()
    }

    override fun openComment() {
        photoView.showCommentUi()
    }

    override fun openUser() {
        photoView.showUserUi()
    }
}