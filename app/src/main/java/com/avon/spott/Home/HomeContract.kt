package com.avon.spott.Home

import com.avon.spott.BaseView
import com.avon.spott.Data.Home


interface HomeContract {

    interface View: BaseView<Presenter>{
        fun showPhotoUi(id:Int)
        fun addItems(homeItems: ArrayList<Home>)
        fun removeLoading()
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun getPhotos(baseUrl:String, start:Int)
    }

}