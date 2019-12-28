package com.avon.spott.Home

import com.avon.spott.BaseView


interface HomeContract {

    interface View: BaseView<Presenter>{
        fun showPhotoUi()
    }

    interface Presenter{
        fun openPhoto()
    }

}