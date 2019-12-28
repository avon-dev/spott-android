package com.avon.spott.Photo

import com.avon.spott.BaseView

class PhotoContract {
    interface View: BaseView<Presenter> {
        fun showPhotoMapUi()
        fun showCommentUi()
        fun showUserUi()
    }

    interface Presenter{
        fun openPhotoMap()
        fun openComment()
        fun openUser()
    }
}