package com.avon.spott.Hashtag

import com.avon.spott.BaseView
import com.avon.spott.Data.HomeItem

interface HashtagContract {
    interface View: BaseView<Presenter> {
        fun showToast(string: String)
        fun showPhotoUi(photoId:Int)
        fun addItems(homeItemItems: ArrayList<HomeItem>)
        fun removePageLoading()
        fun clearAdapter()
        fun showNohashtag(boolean: Boolean)


        var hasNext: Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openPhoto(id:Int)
        fun getPhotos(baseUrl:String, start:Int, hashtag:String, fromSearch:Boolean)

    }
}