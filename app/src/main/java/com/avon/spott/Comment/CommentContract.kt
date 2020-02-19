package com.avon.spott.Comment

import android.app.AlertDialog
import com.avon.spott.BaseView
import com.avon.spott.Data.Comment

interface CommentContract {

    interface View: BaseView<Presenter> {
        fun showUserUi(userId:Int)
        fun showHashtagUi(hashtag:String)
        fun removePageLoading()
        fun clearAdapter()
        fun addItems(commentItems:ArrayList<Comment>)
        fun enableSending(boolean: Boolean)
        fun postDone()
        fun showToast(string: String)
        fun updateDone(alertDialog: AlertDialog, position: Int, content: String)
        fun deleteDone(alertDialog: AlertDialog, position: Int)
        fun setHashCaption(text:String, hashList:ArrayList<Array<Int>>)
        fun setCaption(text: String)
        fun reportDone(alertDialog: AlertDialog, position:Int)
        fun serverError()


        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openUser(userId:Int)
        fun openHashtag(hashtag: String)
        fun getComments(baseurl:String, start:Int, photoId:Int)
        fun checkEditString(string:String)
        fun postCommnet(baseurl: String, photoId: Int, caption:String)
        fun updateComment(baseurl: String, photoId: Int, commentId:Int,
                          alertDialog: AlertDialog, position: Int, content: String)
        fun deleteComment(baseurl: String, photoId: Int, commentId: Int,
                          alertDialog: AlertDialog, position: Int)
        fun getHash(text:String)
        fun report(baseurl: String, reason:Int, detail:String, postId:Int, contents:String,
                   commentId:Int, alertDialog: AlertDialog, position:Int)
    }

}