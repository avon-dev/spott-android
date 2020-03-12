package com.avon.spott.Splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.avon.spott.Data.IntResult
import com.avon.spott.Data.Token
import com.avon.spott.Login.LoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.*
import retrofit2.HttpException


class SplashActivity : Activity() {

    val TAG = "forSplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            // 자동 로그인
            // 1. shared확인
            // 2-1. main으로 이동
            // 2-2. 로그인 액티비티 실행
            val shared = MySharedPreferences(this)
            val access = shared.token
            val refresh = shared.refresh
            if(!access.equals("") and !refresh.equals("")) { // 토큰이 있으면
                availableToken(getString(R.string.baseurl), "/spott/token/verify", Token(refresh, access))
            }else{
                showMainUi(false)
            }
        }, 1000) //로딩 주기
    }

    @SuppressLint("CheckResult")
    private fun availableToken(baseUrl:String, url:String, token: Token) {
        val sending = Parser.toJson(token)

        var auto = false

        Retrofit(baseUrl).postFieldNonHeader(url, sending).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<IntResult>(it) }
            if(result != null) {
                logd(TAG, "result code: ${result.result}")
                // 액세스 토큰 활성 1002
                if (result.result == 1002) {
                    logd(TAG, "토큰 사용 가능")
                    auto = true
                } else if (result.result == 1003) {
                    // 액세스 토큰 만료 1003
                    App.prefs.token = result.access.toString()
                    logd(TAG, "액세스 만료")
                    auto = true

                } else if (result.result == 1001) {
                    // 리프레시 토큰 만료 1001 -> 그냥 로그인 로직을 타야 함
                    logd(TAG, "리프레시 만료")
                }
            }
        }, { throwable ->
            showMainUi(auto)
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, message: ${throwable.message()}")
            }
        }, {
            showMainUi(auto)
        })
    }

    private fun showMainUi(auto:Boolean) {
        logd(TAG, "showMainUi")
        var intent:Intent
        if(auto) {
            intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            intent = Intent(this@SplashActivity, LoginActivity::class.java)
        }

        startActivity(intent)
    }
}
