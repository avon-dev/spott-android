package com.avon.spott.Comment

class CommentPresenter (val commentView:CommentContract.View) : CommentContract.Presenter{
    init{ commentView.presenter = this}

    override fun openPhoto() {
        commentView.showPhotoUi()
    }
}