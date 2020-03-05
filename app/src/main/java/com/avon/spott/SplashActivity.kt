package com.avon.spott

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.avon.spott.Login.LoginActivity

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            Intent(this@SplashActivity, LoginActivity::class.java).let { startActivity(it) }
            finish()
        }, 1000) //로딩 주기

    }
}
