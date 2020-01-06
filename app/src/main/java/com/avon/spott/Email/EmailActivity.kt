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

    private var isFirst: Boolean = true
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val resendTime: Long = 60
    private var number: Number = Number(false)
    private var isEmail: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()

//        temp()
    }

    fun temp() {
        btn_confirm_email_a.visibility = View.VISIBLE
        edit_number_email_a.visibility = View.VISIBLE
        edit_number_email_a.setText("0")
        this.number = Number(true, "0123")
        edit_email_email_a.setText("back947@naver.com")
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

        edit_number_email_a.addTextChangedListener {
            presenter.isNumber(it.toString())
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }
            R.id.btn_send_email_a -> { // 이메일 전송

                if(!isEmail) {
                    Toast.makeText(this@EmailActivity, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return
                }

                presenter.sendEmail(edit_email_email_a.text.toString())
            }
            R.id.btn_confirm_email_a -> {
                if (number.code.toString().equals(edit_number_email_a.text.toString()))
                    presenter.openPassword()
                else {
                    presenter.checkNumber(edit_number_email_a.text.toString())
                }
            }
        }
    }


    override fun navigateUp() {// 뒤로가기
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) { // 이메일 입력에따른 경고문구
        isEmail = valid
        if (isEmail) text_warnmsg1_email_a.visibility = View.INVISIBLE
        else text_warnmsg1_email_a.visibility = View.VISIBLE
    }

    override fun isNumber(bool: Boolean) {
        if(bool)
            btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_primary)
        else
            btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)
    }

    override fun showPasswordUi() { // 인증번호 입력으로 이동
        val intent = Intent(this, PasswordActivity::class.java)
        val user = User(edit_email_email_a.text.toString())
        intent.putExtra(INTENT_EXTRA_USER, user)
        startActivity(intent)
    }


    override fun getNumber(number: Number) {

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

        Toast.makeText(this@EmailActivity, number.code.toString(), Toast.LENGTH_SHORT).show()

//        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다", Toast.LENGTH_SHORT).show()
    }

    override fun showError(msg: String) {
        Toast.makeText(this@EmailActivity, msg, Toast.LENGTH_SHORT).show()
    }
}
