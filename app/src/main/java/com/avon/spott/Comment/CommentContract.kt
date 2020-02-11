package com.avon.spott.Comment

import android.app.AlertDialog
import com.avon.spott.BaseView
import com.avon.spott.Data.Comment

interface CommentContract {

    interface View: BaseView<Presenter> {
        fun showUserUi(userId:Int)
        fun removePageLoading()
        fun clearAdapter()
        fun addItems(commentItems:ArrayList<Comment>)
        fun enableSending(boolean: Boolean)
        fun postDone()
        fun showToast(string: String)
        fun updateDone(alertDialog: AlertDialog, position: Int, content: String)
        fun deleteDone(alertDialog: AlertDialog, position: Int)

        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openUser(userId:Int)
        fun getComments(baseurl:String, start:Int, photoId:Int)
        fun checkEditString(string:String)
        fun postCommnet(baseurl: String, photoId: Int, caption:String)
        fun updateComment(baseurl: String, photoId: Int, commentId:Int,
                          alertDialog: AlertDialog, position: Int, content: String)
        fun deleteComment(baseurl: String, photoId: Int, commentId: Int,
                          alertDialog: AlertDialog, position: Int)
    }

}