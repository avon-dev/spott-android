package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.toolbar.*


class EmailActivity : AppCompatActivity(), EmailContract.View, View.OnClickListener {

    override lateinit var presenter: EmailContract.Presenter
    private lateinit var signUpPresenter: EmailPresenter

    // 유효성 검사 객체
    // private val validator by lazy { Validator.getInstance() }

    private var isFirst: Boolean = true
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private var isEmail: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()
    }

    override fun onDestroy() {
        super.onDestroy()
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

        // 이메일 입력변화 리스너 등록
        edit_email_email_a.addTextChangedListener {
            presenter.isEmail(it.toString())
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }
            R.id.btn_confirm_email_a -> { // 비밀번호 화면으로 이동
                presenter.openPassword()
            }
            R.id.btn_send_email_a -> { // 이메일 전송
                presenter.sendEmail()
            }
        }
    }


    override fun navigateUp() {// 뒤로가기
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) { // 이메일 입력에따른 경고문구
        isEmail = valid
        if (isEmail) text_warnmsg1_email_a.visibility = View.VISIBLE
        else text_warnmsg1_email_a.visibility = View.INVISIBLE
    }


    override fun showPasswordUi() { // 인증번호 입력으로 이동
        val intent = Intent(this@EmailActivity, PasswordActivity::class.java)
        startActivity(intent)
    }


    override fun sendEmail() { // 이메일 전송하기
        if (isFirst) {
            isFirst = false
            startTime = System.currentTimeMillis()
            btn_send_email_a.text = getString(R.string.resend)
            btn_send_email_a.setBackgroundResource(R.drawable.corner_round_primary)

            // 인증번호 입력할 수 있는 뷰 표시
            edit_number_email_a.visibility = View.VISIBLE
            text_numbermessage_email_a.visibility = View.VISIBLE
            btn_confirm_email_a.visibility = View.VISIBLE

            Toast.makeText(this@EmailActivity, "전송", Toast.LENGTH_SHORT).show()
        } else {
            elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime < 60000) {
                Toast.makeText(this@EmailActivity, "1분 후 재전송할 수 있습니다", Toast.LENGTH_SHORT).show()
            } else {
                startTime = System.currentTimeMillis()
                Toast.makeText(this@EmailActivity, "전송", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
