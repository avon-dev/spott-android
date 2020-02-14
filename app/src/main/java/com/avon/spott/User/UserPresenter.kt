package com.avon.spott.User

import com.avon.spott.Data.MypageResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.google.android.gms.maps.model.LatLng
import retrofit2.HttpException

class UserPresenter (val userView:UserContract.View):UserContract.Presenter {

    private val TAG = "UserPresenter"

    init{userView.presenter = this}

    override fun openPhoto(id:Int) {
        userView.showPhotoUi(id)
    }

    override fun getUserphotos(baseurl: String, userId: Int) {
        logd(TAG, "userId : " +userId.toString())

        Retrofit(baseurl).get(App.prefs.temporary_token, "/spott/mypage/"+userId.toString(),  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<MypageResult>(string!!)

                userView.setUserInfo(result.user.nickname, result.user.profile_image)

                if(result.posts.size==0){
                    userView.noPhoto()
//                    userView.movePosition(LatLng(37.56668, 126.97843), 14f) //등록한 사진이 없으면 구글맵 카메라 서울시청으로 이동
                }
//                else{
//                    userView.movePosition(LatLng(result.posts[0].latitude, result.posts[0].longitude), 11f)
//                    //등록한 사진이 있으면 가장 최신 등록된 사진의 위치로 이동
//                }

                userView.addItems(result.posts)

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
}