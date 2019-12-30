package com.avon.spott.Map

import com.avon.spott.BaseView

interface MapContract {
    interface View: BaseView<Presenter> {
        fun showPhotoUi()
    }

    interface Presenter{
        fun openPhoto()
    }
}