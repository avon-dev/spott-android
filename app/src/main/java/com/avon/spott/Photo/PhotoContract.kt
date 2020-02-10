package com.avon.spott.Photo

import com.avon.spott.BaseView

class PhotoContract {
    interface View: BaseView<Presenter> {
        fun showPhotoMapUi(lat:Float, lng:Float, photoUrl:String)
        fun showCommentUi()
        fun showUserUi()
        fun showToast(string: String)
        fun setPhotoDetail(userPhoto:String?, userNickName:String, postPhotoUrl: String,
                           backPhotoUrl:String, photoLat:Double, photoLng:Double,
                           caption:String, comments:Int, dateTime:String, likeCount:Int,
                           likeChecked:Boolean, scrapChecked:Boolean)
        fun showPhotoEnlagement(photoUrl: String)
        fun likeResultDone(count:Int)
        fun likeResultError()
        fun scrapResultDone()
        fun scrapResultError()

    }

    interface Presenter{
        fun openPhotoMap(lat:Double, lng:Double, photoUrl:String)
        fun openComment()
        fun openUser()
        fun openPhotoEnlargement(photoUrl: String)
        fun getPhotoDetail(baseUrl:String, photoId:Int)
        fun postLike(baseUrl: String, photoId: Int)
        fun deleteLike(baseUrl: String, photoId: Int)
        fun postScrap(baseUrl: String, photoId: Int)
        fun deleteScrap(baseUrl: String, photoId: Int)

    }
}