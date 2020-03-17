package com.avon.spott.Recommend

import com.avon.spott.BaseView
import com.avon.spott.Data.HomeItem

interface RecommendContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi(photoId: Int)
        fun addItems(homeItems: ArrayList<HomeItem>)
        fun clearAdapter()

    }

    interface Presenter {
        fun openPhoto(photoId: Int)
        fun getPhotos(baseUrl:String)
    }
}