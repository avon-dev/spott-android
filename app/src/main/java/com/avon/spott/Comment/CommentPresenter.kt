package com.avon.spott.Comment

import android.app.AlertDialog
import com.avon.spott.Data.*
import com.avon.spott.Utils.*
import retrofit2.HttpException

class CommentPresenter (val commentView:CommentContract.View) : CommentContract.Presenter{

    private val TAG = "CommentPresenter"

    init{ commentView.presenter = this}

    override fun openUser(userId:Int) {
        commentView.showUserUi(userId)
    }

    override fun openHashtag(hashtag: String) {
        var sending = hashtag.substring(1) // #제거
        commentView.showHashtagUi(sending)
    }

    override fun getComments(baseurl: String, start: Int, photoId: Int, comeFrom:Int) {

        val homePaging = commentPaging(start, commentView.refreshTimeStamp, comeFrom)
        logd(TAG, "파서테스트 = " + Parser.toJson(homePaging))
        Retrofit(baseurl).get(App.prefs.token,"/spott/posts/"+photoId.toString()+"/comment", Parser.toJson(homePaging))
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

                if(comeFrom==2000){
                    /***************알림에서 왔을 때 분기 처리해줘야함. **************/
                    commentView.setPhotoData(result.notice_data.contents, result.notice_data.user_profile_image,
                        result.notice_data.user_nickname, DateTimeFormatter.formatCreated(result.notice_data.created), result.notice_data.user)
                }

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

        Retrofit(baseurl).post(App.prefs.token,"/spott/posts/"+photoId.toString()+"/comment", Parser.toJson(sending))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    commentView.postDone()
                }else{
                    commentView.showFailsComment(1)
                }

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                commentView.showFailsComment(1)
            })
    }


    override fun checkEditString(string:String){
        if(string.trim().length>0){
            commentView.enableSending(true)
        }else{
            commentView.enableSending(false)
        }
    }

    override fun updateComment(baseurl: String, photoId: Int, commentId:Int,
                               alertDialog: AlertDialog, position: Int, content: String){

        val sending = CommentUpdate(content)
        logd(TAG, "URL은? "+"/spott/posts/"+photoId.toString()+"/comment/"+commentId.toString())
        logd(TAG, "Sending은? "+Parser.toJson(sending))

        Retrofit(baseurl).patch(App.prefs.token,"/spott/posts/"+photoId.toString()+"/comment/"+commentId.toString(),
            Parser.toJson(sending))

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    commentView.updateDone(alertDialog, position, content)
                }else{
                    commentView.showFailsComment(2)
                }

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                commentView.showFailsComment(2)
            })

    }

    override fun deleteComment(baseurl: String, photoId: Int, commentId: Int,
                      alertDialog: AlertDialog, position: Int){
        Retrofit(baseurl).delete(App.prefs.token,"/spott/posts/"+photoId.toString()+"/comment/"+commentId.toString(),
            "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    commentView.deleteDone(alertDialog, position)
                }else{
                    commentView.showFailsComment(3)
                }

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                commentView.showFailsComment(3)
            })
    }

    override fun getHash(text:String){
        val hashArrayList = ArrayList<Array<Int>>()

        val matcher = Validator.validHashtag(text)
        while (matcher.find()){
            if(matcher.group(1) != "") {
                val currentSapn =arrayOf(matcher.start(), matcher.end())
                hashArrayList.add(currentSapn)
            }
        }

        if(hashArrayList.size>0) {
            commentView.setHashCaption(text, hashArrayList)
        }else{
            commentView.setCaption(text)
        }

    }

    override fun  report(baseurl: String, reason:Int, detail:String, postId:Int, contents:String,
                              commentId:Int, alertDialog: AlertDialog,position:Int){
        val reportComment = ReportComment(contents, commentId, postId, detail, reason)
        val sending = Parser.toJson(reportComment)
        logd(TAG, "sending : $sending")

        Retrofit(baseurl).post(App.prefs.token, "spott/report",  sending)
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)
                if(result.result){
                    commentView.reportDone(alertDialog,position)
                }else{
                    commentView.serverError()
                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                commentView.serverError()
            })
    }
}