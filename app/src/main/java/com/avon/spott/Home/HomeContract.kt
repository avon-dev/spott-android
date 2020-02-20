package com.avon.spott.Home

import com.avon.spott.BaseView
import com.avon.spott.Data.HomeItem


interface HomeContract {

    interface View: BaseView<Presenter>{
        fun showPhotoUi(id:Int)
        fun addItems(homeItemItems: ArrayList<HomeItem>)
        fun removePageLoading()
        fun clearAdapter()
        fun showSearchUi()

        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun openSearch()
        fun getPhotos(baseUrl:String, start:Int)
        fun getToken(baseUrl: String, start:Int)
    }

}