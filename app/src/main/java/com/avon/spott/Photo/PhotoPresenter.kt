package com.avon.spott.Photo

import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.LikeScrapResult
import com.avon.spott.Data.PhotoResult
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
        photoView.showPhotoEnlagement(photoUrl)
    }

    override fun getPhotoDetail(baseUrl:String, photoId:Int){
        Retrofit(baseUrl).get(App.prefs.temporary_token, "/spott/posts/"+photoId,  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<PhotoResult>(string!!)

                val hashArrayList = ArrayList<Array<Int>>()

                val matcher = Validator.validHashtag(result.contents)
                while (matcher.find()){
                    if(matcher.group(1) != "") {
//                        val hashtag = "#" + matcher.group(1)
//                        logd("hashhash", "hash"+hashtag)
//
//                        hashArrayList.add(hashtag)
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

    override fun openEditCaption() {
        photoView.showEditCaptionUi()
    }

    override fun deletePhoto(baseUrl: String, photoId: Int) {
        Retrofit(baseUrl).delete(App.prefs.temporary_token, "/spott/posts/"+photoId,  "")
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


}