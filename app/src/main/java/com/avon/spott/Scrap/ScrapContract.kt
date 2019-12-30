package com.avon.spott.Scrap

import com.avon.spott.BaseView

interface ScrapContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
    }

    interface Presenter{
        fun openPhoto()
    }

}