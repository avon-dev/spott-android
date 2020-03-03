package com.avon.spott

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.avon.spott.Login.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            Intent(this@SplashActivity, LoginActivity::class.java).let { startActivity(it) }
            finish()
        }, 1000) //로딩 주기

    }
}
