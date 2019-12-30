package com.avon.spott.Comment

import com.avon.spott.BaseView

interface CommentContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
    }

    interface Presenter{
        fun openPhoto()
    }

}