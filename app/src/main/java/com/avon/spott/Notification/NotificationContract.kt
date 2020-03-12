package com.avon.spott.Notification

import android.app.AlertDialog
import com.avon.spott.BaseView
import com.avon.spott.Data.NotiItem

interface NotificationContract {

    interface View: BaseView<Presenter> {
        fun showPhotoUi(photoId:Int)
        fun showCommentUi(photoId:Int)
        fun showReasonUi(notiId:Int,kind:Int, reason:String)
        fun showUserUi(userId: Int)
        fun addItems(notiItems: ArrayList<NotiItem>)
        fun removePageLoading()
        fun clearAdapter()
        fun deleteDone(position:Int)
        fun serverError()
        fun showFail(state:Int)
        fun showNoNoti(boolean: Boolean)


        var hasNext : Boolean
        var refreshTimeStamp:String
    }

    interface Presenter{
        fun openPhoto(photoId:Int)
        fun openComment(photoId:Int)
        fun openReason(notiId:Int,kind:Int, reason:String)
        fun openUser(userId:Int)
        fun getNoti(baseUrl:String, start:Int)
        fun deleteNoti(baseUrl: String, notiId: Int, postion: Int)
    }
}