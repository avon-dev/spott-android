package com.avon.spott.Signup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.NicknameActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.toolbar.*


class SignupActivity : AppCompatActivity(), SignupContract.View, View.OnClickListener {

    override lateinit var presenter: SignupContract.Presenter
    private lateinit var signUpPresenter:SignupPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        init()
    }

    // 초기화
    private fun init() {
        // 프레젠터 생성
        signUpPresenter = SignupPresenter(this)

        // 회원가입
        text_title_toolbar.text = getString(R.string.signup_)

        // 버튼  클릭 리스너
        img_back_toolbar.setOnClickListener(this)
        btn_confirm_signup_a.setOnClickListener(this)
    }

    // 닉네임 생성으로 이동
    override fun showNicknameUi() {
        val intent = Intent(this@SignupActivity, NicknameActivity::class.java)
        startActivity(intent)
    }

    // 뒤로가기
    override fun navigateUp() { onBackPressed() }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp() }
            R.id.btn_confirm_signup_a -> { presenter.openNickname() }
        }
    }
}
