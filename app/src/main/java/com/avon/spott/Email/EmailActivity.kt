package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.Number
import com.avon.spott.Data.User
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.toolbar.*


class EmailActivity : AppCompatActivity(), EmailContract.View, View.OnClickListener {

    override lateinit var presenter: EmailContract.Presenter
    private lateinit var signUpPresenter: EmailPresenter

    val INTENT_EXTRA_USER = "user"

    // 유효성 검사 객체
    // private val validator by lazy { Validator.getInstance() }

    private var isFirst: Boolean = true
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val resendTime: Long = 60
    private var number: Number = Number(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()

//        temp()
    }

    fun temp() {
        btn_confirm_email_a.visibility = View.VISIBLE
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
        btn_confirm_email_a.setOnClickListener(this)

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
            R.id.btn_send_email_a -> { // 이메일 전송
                if (isFirst) {
                    presenter.sendEmail(edit_email_email_a.text.toString())
                } else {
                    elapsedTime = (System.currentTimeMillis() - startTime) / 1000
                    if (elapsedTime < resendTime) {
                        Toast.makeText(
                            this@EmailActivity,
                            "${resendTime - elapsedTime}초후 재전송할 수 있습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        presenter.sendEmail(edit_email_email_a.text.toString())
                    }
                }
            }
            R.id.btn_confirm_email_a -> { // 인증 성공
                if (number.number.toString().equals(edit_number_email_a.text.toString()))
                    presenter.openPassword()
            }
        }
    }


    override fun navigateUp() {// 뒤로가기
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) { // 이메일 입력에따른 경고문구
        if (valid) text_warnmsg1_email_a.visibility = View.INVISIBLE
        else text_warnmsg1_email_a.visibility = View.VISIBLE
    }


    override fun showPasswordUi() { // 인증번호 입력으로 이동
        val intent = Intent(this, PasswordActivity::class.java)
        val user = User(edit_email_email_a.text.toString())
        intent.putExtra(INTENT_EXTRA_USER, user)
        startActivity(intent)
    }


    override fun sendEmail(number: Number) { // 이메일 전송하기
        if (isFirst) {
            isFirst = false
            btn_send_email_a.text = getString(R.string.resend)

            // 인증번호 입력할 수 있는 뷰 표시
            edit_number_email_a.visibility = View.VISIBLE
            text_numbermessage_email_a.visibility = View.VISIBLE
            btn_confirm_email_a.visibility = View.VISIBLE
        }

        this.number = number
        startTime = System.currentTimeMillis()

        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다", Toast.LENGTH_SHORT).show()
    }


}
