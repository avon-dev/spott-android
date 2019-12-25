package com.avon.spott.Login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.EmailLoginActivity
import com.avon.spott.R
import com.avon.spott.SignupActivity
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

        // 프레젠터 생성하기
        loginPresenter = LoginPresenter(this)

        // 클릭 리스너 등록하기
        setOnClickListener()
    }

    // 버튼 클릭 리스너
    fun setOnClickListener() {
        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)
    }

    // 이메일로그인으로 이동하기
    override fun showEmailLoginUi() {
        val intent = Intent(this@LoginActivity, EmailLoginActivity::class.java)
        startActivity(intent)
    }

    // 회원가입으로 이동하기
    override fun showSignupUi() {
        val intent = Intent(this@LoginActivity, SignupActivity::class.java)
        startActivity(intent)
    }

    // 버튼에 따른 클릭이벤트
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_emaillogin_login_a -> { presenter.openEmailLogin() }
            R.id.text_signup_login_a -> { presenter.openSignup() }
        }
    }
}
