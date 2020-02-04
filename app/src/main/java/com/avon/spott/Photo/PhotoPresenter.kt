package com.avon.spott.Photo

class PhotoPresenter (val photoView:PhotoContract.View) : PhotoContract.Presenter{
    init{ photoView.presenter = this}

    override fun openPhotoMap(lat:Double, lng:Double, photoUrl:String) {
        photoView.showPhotoMapUi(lat.toFloat(), lng.toFloat(), photoUrl)
    }

    override fun openComment() {
        photoView.showCommentUi()
    }

    override fun openUser() {
        photoView.showUserUi()
    }
}