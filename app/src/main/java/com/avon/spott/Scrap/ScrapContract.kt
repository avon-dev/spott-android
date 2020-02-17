package com.avon.spott.Scrap

import com.avon.spott.BaseView
import com.avon.spott.Data.ScrapItem

interface ScrapContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi(id: Int)
        fun showCameraUi(photoUrl:String)
        fun addItems(scrapItems:ArrayList<ScrapItem>)
        fun showReady(boolean: Boolean)
        fun deleteDone(scrapItems: ArrayList<ScrapItem>)
        fun deleteError()
        fun showToast(string: String)
        fun clearAdapter()
    }

    interface Presenter{
        fun openPhoto(id: Int)
        fun openCamera(photoUrl:String)
        fun getScraps(baseUrl:String)
        fun deleteScraps(baseUrl: String, scrapItems:ArrayList<ScrapItem>)
    }

}