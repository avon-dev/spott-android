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

                userView.clearAdapter()

                userView.setUserInfo(result.user.nickname, result.user.profile_image)

                if(result.posts.size==0){
                    userView.noPhoto()
                }


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