package com.avon.spott.EditMyinfo

import android.annotation.SuppressLint
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.UserInfo
import com.avon.spott.Utils.*
import retrofit2.HttpException

class EditMyInfoPresenter(val editMyInfoView:EditMyInfoContract.View) : EditMyInfoContract.Presenter {

    private val TAG = "EditMyInfoPresenter"

    init {
        editMyInfoView.presenter = this
    }

    override fun navigateUp() {
        editMyInfoView.navigateUp()
    }

    @SuppressLint("CheckResult")
    override fun getUser(baseUrl: String, token: String) { // 유저정보 가져오기
        Retrofit(baseUrl).get(token, "/spott/users").subscribe({ response ->
            logd(TAG, "response : ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<UserInfo>(it)}
            if(result != null) editMyInfoView.getUserInfo(result)
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun setNickname(baseUrl:String, token:String, nickname: String) {
        val userInfo = UserInfo()
        userInfo.nickname = nickname
        val sending = Parser.toJson(userInfo)
        Retrofit(baseUrl).patch(token, "/spott/users", sending).subscribe({ response ->
            logd(TAG, "response : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if(result != null)
                editMyInfoView.getNickname(result.result)
        }, {

        })
    }

    override fun signOut(pref:MySharedPreferences) { // 로그아웃하기
        EditMyInfoModel.deleteToken(pref)
        editMyInfoView.loginActivity()
    }
}