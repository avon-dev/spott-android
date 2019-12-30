package com.avon.spott.Main

import com.avon.spott.BaseView

interface MainContract{
    interface View:BaseView<Presenter>{
        fun navigateUp()
        fun showCameraUi()
    }

    interface Presenter{
        fun navigateUp()
        fun openCamera()
    }
}