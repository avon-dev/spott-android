package com.avon.spott.User

import com.avon.spott.BaseView

interface UserContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
    }

    interface Presenter{
        fun openPhoto()

    }
}