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

/*
    남은 작업
    * 소셜로그인
    * 이용약관
 */
class LoginActivity : AppCompatActivity(), LoginContract.View, View.OnClickListener {

    private lateinit var loginPresenter: LoginPresenter // 프레젠터
    override lateinit var presenter: LoginContract.Presenter // 프레젠터 인터페이스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 초기화
        init()
    }

    // 초기화
    private fun init() {
        // 프레젠터 생성
        loginPresenter = LoginPresenter(this)

        // 버튼 클릭 리스너
        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)

        val span: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() { // 이용약관
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, MainActivity::class.java).let { startActivity(it) }
            }
        }, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        val span2: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() { // 개인정보수집 이용약관
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, EmailLoginActivity::class.java).let { startActivity(it) }
            }
        }, 20, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()
    }

    // 이메일로그인으로 이동하기
    override fun showEmailLoginUi() {
        val intent = Intent(this@LoginActivity, EmailLoginActivity::class.java)
        startActivity(intent)
    }

    // 회원가입으로 이동하기
    override fun showSignupUi() {
        val intent = Intent(this@LoginActivity, EmailActivity::class.java)
        startActivity(intent)
    }

    // 버튼에 따른 클릭이벤트
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
