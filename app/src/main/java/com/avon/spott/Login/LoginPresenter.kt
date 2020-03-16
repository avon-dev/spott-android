package com.avon.spott.Login

import android.annotation.SuppressLint
import com.avon.spott.Data.CheckSignupResult
import com.avon.spott.Data.SocialUser
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.loge

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
    override fun getToken() {

        // 토큰발급 받으면 메인으로 이동하기
        loginView.showMainUi()
    }
}