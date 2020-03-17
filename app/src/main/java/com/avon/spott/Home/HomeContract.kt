package com.avon.spott.Home

import com.avon.spott.BaseView
import com.avon.spott.Data.HomeItem


interface HomeContract {

    interface View: BaseView<Presenter>{
        fun showPhotoUi(id:Int)
        fun addItems()
        fun removePageLoading()
        fun clearAdapter()
        fun showSearchUi()
        fun showRecommendUi()
        fun loadNativeAds(homeItemItems: ArrayList<HomeItem>, pagable:Boolean)

        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun openSearch()
        fun openRecommend()
        fun getPhotos(baseUrl:String, start:Int, action:Int)
//        fun getToken(baseUrl: String, start:Int, action:Int)
    }

}