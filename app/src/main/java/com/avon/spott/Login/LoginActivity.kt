package com.avon.spott.Login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Email.EmailActivity
import com.avon.spott.EmailLogin.EmailLoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), LoginContract.View, View.OnClickListener {

    private lateinit var loginPresenter: LoginPresenter
    override lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
    }

    // 초기화
    private fun init() {
        loginPresenter = LoginPresenter(this)

        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)

        val span: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, MainActivity::class.java).let { startActivity(it) }
            }
        }, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        val span2: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, EmailLoginActivity::class.java).let { startActivity(it) }
            }
        }, 20, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun showEmailLoginUi() {
        val intent = Intent(this@LoginActivity, EmailLoginActivity::class.java)
        startActivity(intent)
    }

    override fun showSignupUi() {
        val intent = Intent(this@LoginActivity, EmailActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_emaillogin_login_a -> {
                presenter.openEmailLogin()
            }
            R.id.text_signup_login_a -> {
                presenter.openSignup()
            }
        }
    }
}
