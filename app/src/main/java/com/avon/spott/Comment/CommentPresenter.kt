package com.avon.spott.Comment

import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.CommentPost
import com.avon.spott.Data.CommentResult
import com.avon.spott.Data.HomePaging
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.Retrofit
import retrofit2.HttpException

class CommentPresenter (val commentView:CommentContract.View) : CommentContract.Presenter{

    private val TAG = "CommentPresenter"

    init{ commentView.presenter = this}

    override fun openPhoto() {
        commentView.showPhotoUi()
    }

    override fun getComments(baseurl: String, start: Int, photoId: Int) {
        val homePaging = HomePaging(start, commentView.refreshTimeStamp)
        logd(TAG, "파서테스트 = " + Parser.toJson(homePaging))
        Retrofit(baseurl).get(App.prefs.temporary_token,"/spott/posts/"+photoId.toString()+"/comment", Parser.toJson(homePaging))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<CommentResult>(string!!)

                if (start ==0){
                    commentView.clearAdapter()
                }else if(commentView.hasNext){
                    commentView.removePageLoading()
                }

                logd(TAG, "페이저블 " + result.pageable)

                commentView.hasNext = result.pageable //페이지가 남아있는지 여부

                commentView.refreshTimeStamp = result.created_time //리프리쉬 타임 설정

                commentView.addItems(result.items) //아이템들 추가


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

    override fun postCommnet(baseurl: String, photoId: Int, caption:String){

        val sending = CommentPost(caption)

        Retrofit(baseurl).post(App.prefs.temporary_token,"/spott/posts/"+photoId.toString()+"/comment", Parser.toJson(sending))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    commentView.postDone()
                }else{

                    commentView.showToast("댓글 등록에 실패했습니다")
                }

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                commentView.showToast("댓글 등록에 실패했습니다")
            })
    }


    override fun checkEditString(string:String){
        if(string.trim().length>0){
            commentView.enableSending(true)
        }else{
            commentView.enableSending(false)
        }
    }
}