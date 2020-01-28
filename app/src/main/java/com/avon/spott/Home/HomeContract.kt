package com.avon.spott.Home

import com.avon.spott.BaseView


interface HomeContract {

    interface View: BaseView<Presenter>{
        fun showPhotoUi(id:Int)
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun getPhotos(baseUrl:String, start:Int)
    }

}