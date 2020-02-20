package com.avon.spott.Notification

import android.app.AlertDialog
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.HomePaging
import com.avon.spott.Data.NotiResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class NotificationPresenter (val notiView: NotificationContract.View) : NotificationContract.Presenter {

    private val TAG = "NotificationPresenter"

    init{ notiView.presenter = this}

    override fun openPhoto(photoId:Int) { notiView.showPhotoUi(photoId) }

    override fun openComment(photoId:Int) { notiView.showCommentUi(photoId) }

    override fun openReason(notiId: Int,kind:Int, reason:String) {
        notiView.showReasonUi(notiId,kind, reason)
    }

    override fun openUser(userId: Int) {
        notiView.showUserUi(userId)
    }

    override fun getNoti(baseUrl: String, start: Int) {

        val paging = HomePaging(start, notiView.refreshTimeStamp)

        Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/notice", Parser.toJson(paging))

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<NotiResult>(string!!)

                if (start ==0){
                    notiView.clearAdapter()
                }else if(notiView.hasNext){
                    notiView.removePageLoading()
                }

                notiView.hasNext = result.pageable //페이지가 남아있는지 여부

                notiView.refreshTimeStamp = result.created_time //리프리쉬 타임 설정

                notiView.addItems(result.items) //아이템들 추가


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
            })

    }

    override fun deleteNoti(baseUrl: String, notiId: Int, position: Int) {
        Retrofit(baseUrl).delete(App.prefs.temporary_token,"/spott/notice/"+notiId.toString(),"")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    notiView.deleteDone(position)
                }else{
                    notiView.showFail(1)
                }

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                notiView.showFail(1)
            })
    }
}