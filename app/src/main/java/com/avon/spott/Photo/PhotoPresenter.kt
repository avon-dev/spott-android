package com.avon.spott.Photo

import android.annotation.SuppressLint
import android.app.AlertDialog
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.LikeScrapResult
import com.avon.spott.Data.PhotoResult
import com.avon.spott.Data.ReportPhoto
import com.avon.spott.Mypage.MypageFragment
import com.avon.spott.Utils.*
import com.avon.spott.Utils.DateTimeFormatter.Companion.convertLocalDate
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

    @SuppressLint("CheckResult")
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

                    // photo에서는 서버 시간 -> 현지 시간으로 변환 -> 방금, 1분전, 1시간전, 년 월 일로 표시

                    photoView.setPhotoDetail(result.user.profile_image, result.user.nickname,
                        result.posts_image, result.back_image, result.latitude, result.longitude,
                        result.contents, result.comment, convertLocalDate(result.created), result.count,
                        result.like_checked, result.scrap_checked, result.myself, result.user.id, hasHash)

//                    DateTimeFormatter.convertLocalDate(result.created)

//                    var now = Date()
//                    var ttime:Date
//                    // 기기 설정 나라 시간
////                    var tz: TimeZone = TimeZone.getDefault() // 현재 기기에서 설정된 timezone
////                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
//                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mmX")
//
//                    // 기기 설정 시간
//                    sdf.timeZone = TimeZone.getDefault()
//                    sdf.parse(result.created)
//                    ttime = sdf.parse(result.created)
//                    println ( sdf.format(ttime) )
//                    println( sdf.format(now) )
//
////                    // 로스 엔젤레스 시간
//                    sdf.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
//                    ttime = sdf.parse(result.created)
//                    sdf.format(ttime)
//                    println ( sdf.format(ttime) )
//
//
//                    sdf.timeZone = TimeZone.getTimeZone("pdofpdop")
//                    ttime = sdf.parse(result.created)
//                    sdf.format(ttime)
//                    println ( sdf.format(ttime) )
//
//                    var location = tz.id // 기기의 위치 얻기
//                    val date = Date()

//                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)")
//                    val tdate = sdf.parse("2020-03-04T22:04:00.456145Z")
//                    val tdate2 = sdf.parse(result.created)


                    // 그냥 Date는 어떤 시간인지
//                logd(TAG, "date:${df.format(date)}")
//                    logd(TAG, "date:${sdf.format(tdate)}")


                }

                // "2020-03-02T11:08:13.456145Z"

//                val ttime = ""
//                // 기기 설정 나라 시간
//                var tz: TimeZone = TimeZone.getDefault() // 현재 기기에서 설정된 timezone
//                var location = tz.id // 기기의 위치 얻기
//                val date = Date()
//
////                val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)")
//                val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
//                val tdate = df.parse("2020-03-02T11:08:13.456145Z")
//
//
//                // 그냥 Date는 어떤 시간인지
////                logd(TAG, "date:${df.format(date)}")
//                logd(TAG, "date:${df.format(tdate)}")


//                // df에 시간 설정
//                df.timeZone = tz
//                logd(TAG, "id:${tz.id}")
//                logd(TAG, "location:${location}")
//                logd(TAG, "${tz.displayName} // ${df.format(date)}")
//
//                // 로스엔젤레스로 설정
//                tz = TimeZone.getTimeZone("America/Los_Angeles")
//                location = tz.id
//                df.timeZone = tz
//                logd(TAG, "id:${tz.id}")
//                logd(TAG, "location:${location}")
//                logd(TAG, "${tz.displayName} // ${df.format(date)}")


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

                photoView.serverError()
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

                photoView.serverError()
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

                photoView.serverError()
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

                photoView.serverError()
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
                    photoView.photoDeleteError()
                }


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }

                photoView.photoDeleteError()

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