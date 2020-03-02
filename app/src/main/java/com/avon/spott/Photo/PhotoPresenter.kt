package com.avon.spott.Photo

import android.app.AlertDialog
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.LikeScrapResult
import com.avon.spott.Data.PhotoResult
import com.avon.spott.Data.ReportPhoto
import com.avon.spott.Mypage.MypageFragment
import com.avon.spott.Utils.*
import com.avon.spott.Utils.DateTimeFormatter.Companion.formatCreated
import retrofit2.HttpException

class PhotoPresenter (val photoView:PhotoContract.View) : PhotoContract.Presenter{

    private val TAG ="PhotoPresenter"

    init{ photoView.presenter = this}

    override fun openPhotoMap(lat:Double, lng:Double, photoUrl:String) {
        photoView.showPhotoMapUi(lat.toFloat(), lng.toFloat(), photoUrl)
    }

    override fun openComment() {
        photoView.showCommentUi()
    }

    override fun openUser() {
        photoView.showUserUi()
    }

    override fun openPhotoEnlargement(photoUrl: String){
        photoView.showPhotoEnlagementUi(photoUrl)
    }

    override fun openHashtag(hashtag: String) {
        var sending = hashtag.substring(1) // #제거
        photoView.showHashtagUi(sending)
    }

    override fun openCamera(postPhotoUrl: String, backPhotoUrl: String?) {
        photoView.showCameraUi(postPhotoUrl, backPhotoUrl)
//        photoView.showCameraUi(photoUrl)
    }

    override fun openEditCaption() {
        photoView.showEditCaptionUi()
    }

    override fun getPhotoDetail(baseUrl:String, photoId:Int){
        Retrofit(baseUrl).get(App.prefs.token, "/spott/posts/"+photoId,  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<PhotoResult>(string!!)

                if(result.result == 1201){
                    photoView.showReportedDialog()
                }else if(result.result == 1200){
                    val hashArrayList = ArrayList<Array<Int>>()

                    val matcher = Validator.validHashtag(result.contents)
                    while (matcher.find()){
                        if(matcher.group(1) != "") {

                            val currentSapn =arrayOf(matcher.start(), matcher.end())
                            hashArrayList.add(currentSapn)
                        }
                    }

                    var hasHash = false
                    if(hashArrayList.size>0){
                        photoView.setCaption(result.contents, hashArrayList)
                        hasHash = true
                    }


                    photoView.setPhotoDetail(result.user.profile_image, result.user.nickname,
                        result.posts_image, result.back_image, result.latitude, result.longitude,
                        result.contents, result.comment, formatCreated(result.created), result.count,
                        result.like_checked, result.scrap_checked, result.myself, result.user.id, hasHash)
                }



            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
                photoView.showNoPhotoDialog()
            })
    }

    override fun postLike(baseUrl: String, photoId: Int){
        Retrofit(baseUrl).post(App.prefs.token, "/spott/posts/"+photoId.toString()+"/likes",  "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<LikeScrapResult>(string!!)
                 if(result.result){
                     photoView.likeResultDone(result.count)
                 }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.showToast("서버 연결에 오류가 발생했습니다")
                photoView.likeResultError()

            })
    }

    override fun deleteLike(baseUrl: String, photoId: Int){
        Retrofit(baseUrl).delete(App.prefs.token, "/spott/posts/"+photoId.toString()+"/likes",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<LikeScrapResult>(string!!)
                logd(TAG, "결과 : "+ result.count)
//                if(result.result){
                    photoView.likeResultDone(result.count)
//                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.showToast("서버 연결에 오류가 발생했습니다")
                photoView.likeResultError()

            })
    }

    override fun postScrap(baseUrl: String, photoId: Int){
        /* 임시 수정 : temporary_token -> token*/
        Retrofit(baseUrl).post(App.prefs.token, "/spott/posts/"+photoId.toString()+"/scrap",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<LikeScrapResult>(string!!)
//                if(result.result){
                    photoView.scrapResultDone()
//                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.showToast("서버 연결에 오류가 발생했습니다")
                photoView.scrapResultError()

            })
    }

    override fun deleteScrap(baseUrl: String, photoId: Int) {
        Retrofit(baseUrl).delete(App.prefs.token, "/spott/posts/"+photoId.toString()+"/scrap",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<LikeScrapResult>(string!!)
//                if(result.result){
                    photoView.scrapResultDone()
//                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.showToast("서버 연결에 오류가 발생했습니다")
                photoView.scrapResultError()
            })
    }


    override fun deletePhoto(baseUrl: String, photoId: Int) {
        Retrofit(baseUrl).delete(App.prefs.token, "/spott/posts/"+photoId,  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)
                if(result.result){
                MypageFragment.mypageChange = true
                photoView.navigateUp()
                }else{
                    photoView.showToast("사진이 삭제되지 않았습니다\n다시 시도하세요")
                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.showToast("사진이 삭제되지 않았습니다\n다시 시도하세요")

            })
    }

    override fun report(
        baseUrl: String,
        reason: Int,
        detail: String,
        postId: Int,
        postUrl: String,
        postCaption: String,
        alertDialog: AlertDialog
    ) {
        val reportPhoto = ReportPhoto(postUrl, postCaption, postId, detail, reason)

        val sending = Parser.toJson(reportPhoto)
        logd(TAG, "sending : $sending")

        Retrofit(baseUrl).post(App.prefs.token, "spott/report",  sending)
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)
                if(result.result){
                    photoView.reportDone(alertDialog)
                    photoView.navigateUp()
                }else{
                   photoView.serverError()
                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.serverError()
            })

    }


}