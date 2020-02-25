package com.avon.spott.User

import com.avon.spott.Data.FromSearch
import com.avon.spott.Data.MypageResult
import com.avon.spott.Search.SearchFragment.Companion.recentChange
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class UserPresenter (val userView:UserContract.View):UserContract.Presenter {

    private val TAG = "UserPresenter"

    init{userView.presenter = this}

    override fun openPhoto(id:Int) {
        userView.showPhotoUi(id)
    }

    override fun getUserphotos(baseurl: String, userId: Int, fromSearch:Boolean) {
        logd(TAG, "userId : " +userId.toString())

        var action = 1202
        if(fromSearch){
            action= 1201
            recentChange = true
        }

        val fromSearch = FromSearch(action)

        logd(TAG, "sending  : " + Parser.toJson(fromSearch))

        Retrofit(baseurl).get(App.prefs.token, "/spott/user/"+userId.toString()+"/posts",  Parser.toJson(fromSearch))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<MypageResult>(string!!)

                userView.clearAdapter()

                userView.setUserInfo(result.user.nickname, result.user.profile_image, result.user.is_public, result.myself)

                if(result.myself){
                    if(result.posts.size==0){
                        userView.noPhoto()
                    }
                }else{
                    if(result.posts.size==0 && result.user.is_public){
                        userView.noPhoto()
                    }
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