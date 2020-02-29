package com.avon.spott.Login

import android.annotation.SuppressLint
import com.avon.spott.Data.IntResult
import com.avon.spott.Data.Token
import com.avon.spott.Utils.*
import retrofit2.HttpException

class LoginPresenter(val loginView:LoginContract.View) : LoginContract.Presenter {

    private val TAG = "LoginPresenter"

    // 프레젠터 생성시 뷰에도 프레젠터 등록하기
    init { loginView.presenter = this }

    // 이메일로그인으로 이동
    override fun openEmailLogin() { loginView.showEmailLoginUi() }

    // 회원가입으로 이동
    override fun openSignup() { loginView.showSignupUi() }

    @SuppressLint("CheckResult")
    override fun availableToken(baseUrl:String, url:String, token: Token) {
        val sending = Parser.toJson(token)

        Retrofit(baseUrl).postFieldNonHeader(url, sending).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<IntResult>(it) }
            if(result != null) {
                logd(TAG, "result code: ${result.result}")
                // 액세스 토큰 활성 1002
                if (result.result == 1002) {
                    loginView.showMainUi()
                } else if (result.result == 1003) {
                    // 액세스 토큰 만료 1003
                    App.prefs.token = result.access.toString()
                    loginView.showMainUi()
                } else if (result.result == 1001) {
                    // 리프레시 토큰 만료 1001 -> 그냥 로그인 로직을 타야 함
                }
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, message: ${throwable.message()}")
            }
        })
    }
}