package com.avon.spott

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Login.LoginActivity

class SplashActivity : AppCompatActivity() {

    val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            Intent(this@SplashActivity, LoginActivity::class.java).let { startActivity(it) }
            finish()
        }, 1000) //로딩 주기

        // 자동 로그인 로직 넣기

    }
}
