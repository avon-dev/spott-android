package com.avon.spott.Comment

import com.avon.spott.BaseView
import com.avon.spott.Data.Comment

interface CommentContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi()
        fun removePageLoading()
        fun clearAdapter()
        fun addItems(commentItems:ArrayList<Comment>)
        fun enableSending(boolean: Boolean)
        fun postDone()
        fun showToast(string: String)

        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openPhoto()
        fun getComments(baseurl:String, start:Int, photoId:Int)
        fun checkEditString(string:String)
        fun postCommnet(baseurl: String, photoId: Int, caption:String)
    }

}