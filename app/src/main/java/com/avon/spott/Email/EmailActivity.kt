package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.toolbar.*


class EmailActivity : AppCompatActivity(), EmailContract.View, View.OnClickListener {

    override lateinit var presenter: EmailContract.Presenter
    private lateinit var signUpPresenter:EmailPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()
    }

    // 초기화
    private fun init() {
        // 프레젠터 생성
        signUpPresenter = EmailPresenter(this)

        // 회원가입
        text_title_toolbar.text = getString(R.string.email)

        // 버튼  클릭 리스너
        img_back_toolbar.setOnClickListener(this)
        btn_confirm_email_a.setOnClickListener(this)
        btn_send_email_a.setOnClickListener(this)
    }

    // 인증번호 입력으로 이동
    override fun showPasswordUi() {
        val intent = Intent(this@EmailActivity, PasswordActivity::class.java)
        startActivity(intent)
    }

    // 뒤로가기
    override fun navigateUp() { onBackPressed() }

    override fun sendEmail() {
        btn_send_email_a.text = getString(R.string.resend)

        edit_number_email_a.visibility = View.VISIBLE
        text_numbermessage_email_a.visibility = View.VISIBLE
        btn_confirm_email_a.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp() }
            R.id.btn_confirm_email_a -> { presenter.openPassword() }
            R.id.btn_send_email_a -> { presenter.send() }
        }
    }
}
