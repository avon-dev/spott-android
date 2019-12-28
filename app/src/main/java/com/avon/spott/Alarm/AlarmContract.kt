package com.avon.spott.Alarm

import com.avon.spott.BaseView

interface AlarmContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
        fun showCommentUi()
    }

    interface Presenter{
        fun openPhoto()
        fun openCommnet()
    }
}