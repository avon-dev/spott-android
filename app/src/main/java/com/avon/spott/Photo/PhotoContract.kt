package com.avon.spott.Photo

import com.avon.spott.BaseView

class PhotoContract {
    interface View: BaseView<Presenter> {
        fun showPhotoMapUi(lat:Float, lng:Float, photoUrl:String)
        fun showCommentUi()
        fun showUserUi()
        fun showToast(string: String)
    }

    interface Presenter{
        fun openPhotoMap(lat:Double, lng:Double, photoUrl:String)
        fun openComment()
        fun openUser()
    }
}