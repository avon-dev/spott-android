package com.avon.spott.EmailLogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.FindPWActivity
import com.avon.spott.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*

class EmailLoginActivity : AppCompatActivity(), EmailLoginContract.View, View.OnClickListener {

    override lateinit var presenter: EmailLoginContract.Presenter
    private lateinit var emailLoginPresenter: EmailLoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        init() // 초기화
    }

    // 초기화
    private fun init() {
        // 프레젠터 생성
        emailLoginPresenter = EmailLoginPresenter(this)

        // 타이틀 설정
        text_title_toolbar.text = getString(R.string.text_title_emaillogin)

        // 버튼 클릭 리스너
        img_back_toolbar.setOnClickListener(this)
        text_findpw_emailogin_a.setOnClickListener(this)
        btn_login_emailogin_a.setOnClickListener(this)
    }

    // 메인으로 이동
    override fun showMainUi() {
        val intent = Intent(this@EmailLoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    // 비밀번호 찾기로 이동
    override fun showFindPWUi() {
        val intent = Intent(this@EmailLoginActivity, FindPWActivity::class.java)
        startActivity(intent)
    }

    // 뒤로가기
    override fun navigateUp() {
        onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            // 뒤로가기
            R.id.img_back_toolbar -> { presenter.navigateUp() }
            R.id.text_findpw_emailogin_a -> { presenter.openFindPW() }
            R.id.btn_login_emailogin_a -> { presenter.openMain() }
        }
    }
}
