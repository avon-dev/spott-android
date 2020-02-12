package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Camera.EMAIL_RESENDING_MILLS
import com.avon.spott.Data.Number
import com.avon.spott.Data.User
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.toolbar.*

const val INTENT_EXTRA_USER = "signup_user"

class EmailActivity : AppCompatActivity(), EmailContract.View, View.OnClickListener {

    private val TAG = "EmailActivity"

    override lateinit var presenter: EmailContract.Presenter
    private lateinit var signUpPresenter: EmailPresenter

    private var isFirst: Boolean = true
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var number: Number = Number(false)
    private var isEmail: Boolean = false

    private var isSending: Boolean = true
    private val resendingTime = EMAIL_RESENDING_MILLS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()
    }

    // 초기화
    private fun init() {
        signUpPresenter = EmailPresenter(this)

        text_title_toolbar.text = getString(R.string.email)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_email_a.setOnClickListener(this)
        btn_send_email_a.setOnClickListener(this)
        btn_confirm_email_a.setOnClickListener(this)

        edit_email_email_a.addTextChangedListener {
            presenter.isEmail(it.toString())
        }

        edit_number_email_a.addTextChangedListener {
            presenter.isNumber(it.toString())
        }
    }


    // 뒤로가기
    override fun navigateUp() {
        onBackPressed()
    }

    // 이메일 확인하기
    override fun validEmail(valid: Boolean) {
        isEmail = valid
        if (isEmail) text_warnmsg1_email_a.visibility = View.INVISIBLE
        else text_warnmsg1_email_a.visibility = View.VISIBLE
    }

    // 인증번호 확인하기
    override fun validNumber(bool: Boolean) {
        if (bool)  btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_primary)
        else btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)
    }

    // 비밀번호 작성 액티비티로 이동하기
    override fun showPasswordUi() {
        val user = User(edit_email_email_a.text.toString())
        val intent = Intent(this, PasswordActivity::class.java).apply {
            putExtra(INTENT_EXTRA_USER, user)
        }
        startActivity(intent)
    }

    // 인증번호 받기
    override fun getNumber(number: Number) {
        this.number = number

        // 처음 인증번호 받을 때
        if (isFirst) {
            isFirst = false
            btn_send_email_a.text = getString(R.string.resend)

            edit_number_email_a.visibility = View.VISIBLE
            text_numbermessage_email_a.visibility = View.VISIBLE
            btn_confirm_email_a.visibility = View.VISIBLE
        }

        if(!isSending) { // 재전송 버튼 사용을 못할 때
            elapsedTime = resendingTime/1000 - (System.currentTimeMillis() - startTime) / 1000
            Toast.makeText(this@EmailActivity, "${elapsedTime}초 후 다시 시도해주세요", Toast.LENGTH_SHORT).show()
            return
        } else { // 재전송 버튼 사용이 가능할 때
            startTime = System.currentTimeMillis()
        }


        btn_send_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)
        isSending = false

        btn_send_email_a.postDelayed({
            btn_send_email_a.setBackgroundResource(R.drawable.corner_round_primary)
            isSending = true
        }, resendingTime)

//        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다", Toast.LENGTH_SHORT).show()

        // 임시 코드
        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다 ${number.code}", Toast.LENGTH_SHORT).show()


    }

    // 에러 보여주기
    override fun showError(msg: String) {
        Toast.makeText(this@EmailActivity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_send_email_a -> {
                presenter.sendEmail(isEmail, getString(R.string.baseurl), edit_email_email_a.text.toString())
            }
            R.id.btn_confirm_email_a -> {
                presenter.confirm(number, edit_number_email_a.text.toString())
            }
        }
    }
}
