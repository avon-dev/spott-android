package com.avon.spott.Photo

import com.avon.spott.Data.LikeScrapResult
import com.avon.spott.Data.PhotoResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.DateTimeFormatter.Companion.formatCreated
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
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
        photoView.showPhotoEnlagement(photoUrl)
    }

    override fun getPhotoDetail(baseUrl:String, photoId:Int){
        Retrofit(baseUrl).get(App.prefs.temporary_token, "/spott/posts/"+photoId,  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<PhotoResult>(string!!)


                photoView.setPhotoDetail(result.user.profile_image, result.user.nickname,
                    result.posts_image, result.back_image, result.latitude, result.longitude,
                    result.contents, result.comment, formatCreated(result.created), result.count,
                    result.like_checked, result.scrap_checked)

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

    override fun postLike(baseUrl: String, photoId: Int){
        Retrofit(baseUrl).post(App.prefs.temporary_token, "/spott/like/"+photoId,  "")
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
        Retrofit(baseUrl).delete(App.prefs.temporary_token, "/spott/like/"+photoId,  "")
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
        Retrofit(baseUrl).post(App.prefs.temporary_token, "/spott/scrap/"+photoId,  "")
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
        Retrofit(baseUrl).delete(App.prefs.temporary_token, "/spott/scrap/"+photoId,  "")
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


}