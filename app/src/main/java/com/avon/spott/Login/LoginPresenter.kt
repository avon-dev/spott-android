package com.avon.spott.Login

import android.annotation.SuppressLint
import com.avon.spott.Data.CheckSignupResult
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Utils.*
import com.google.gson.GsonBuilder
import retrofit2.HttpException

class LoginPresenter(val loginView:LoginContract.View) : LoginContract.Presenter {

    private val TAG = "LoginPresenter"

    // 프레젠터 생성시 뷰에도 프레젠터 등록하기
    init { loginView.presenter = this }

    // 이메일로그인으로 이동
    override fun openEmailLogin() { loginView.showEmailLoginUi() }

    // 회원가입으로 이동
    override fun openSignup() { loginView.showSignupUi() }


    // 가입한 유저인지 체크 ( 소셜 로그인 )
    @SuppressLint("CheckResult")
    override fun isPhopoUser(baseUrl: String, url: String, socialUser: SocialUser) {

        val sending = Parser.toJson(socialUser)
        logd(TAG, "isPhopoUser(sending): $sending")

        Retrofit(baseUrl).getNonHeader(url, sending).subscribe({
            val result = it.body()?.let { it1 -> Parser.fromJson<CheckSignupResult>(it1) }
            logd(TAG, "isPhopoUser() : $result")

            result?.sign_up?.let {
                if(it) {
                    loginView.notPhopoUser()
                } else {
                    loginView.isPhopoUser()
                }
            }


        }, { throwable ->
            loge(TAG, "throwable: ${throwable.message}")
        })
    }

    // 토큰 발급 받기
    @SuppressLint("CheckResult")
    override fun getToken(baseUrl: String, url: String, socialUser: SocialUser) {
        logd(TAG, "${socialUser.email}, ${socialUser.user_type}")

        Retrofit(baseUrl).signIn("/spott/token", socialUser.email, socialUser.user_type!!).subscribe({ response ->
            logd(TAG, "getToken() : response code: ${response.code()}, response body : ${response.body()}")

            val jsonObj = response.body()
            if (jsonObj != null) {
                val token = GsonBuilder().create().fromJson(jsonObj, Token::class.java)
                if(!token.access.equals("") && !token.refresh.equals("")) {
                    loginView.showMainUi(token)
                }
            }
        }, { throwable ->
            loge(TAG, "getToken(): ${throwable.message}")
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
                loginView.showMessage(throwable.code())
            } else {
                loginView.showMessage(App.ERROR_ERTRY)
            }
        })
    }
}