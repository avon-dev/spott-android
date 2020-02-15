package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Camera.EMAIL_FIND_RESENDING_MILLS
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


    private var transmitable = true // 이메일을 보낼 수 있는 상태인지
    private var startTime = 0L
    private val resending = EMAIL_FIND_RESENDING_MILLS
    private var isShowing = false


    private var isFirst: Boolean = true
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
        if (valid) { // 이메일이 맞을 때
            text_warnmsg1_email_a.visibility = View.INVISIBLE
            if (transmitable)
                btn_send_email_a.setBackgroundResource(R.drawable.corner_round_primary)
        } else { // 이메일이 아닐 때
            text_warnmsg1_email_a.visibility = View.VISIBLE
            if (transmitable)
                btn_send_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)
        }


//        isEmail = valid
//        if (isEmail) text_warnmsg1_email_a.visibility = View.INVISIBLE
//        else text_warnmsg1_email_a.visibility = View.VISIBLE
    }

    // 인증번호 확인하기
    override fun validNumber(bool: Boolean) {
        if (bool) btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_primary)
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

        hideLoading()

        if (!isShowing) {
            edit_number_email_a.visibility = View.VISIBLE
            text_numbermessage_email_a.visibility = View.VISIBLE
            btn_confirm_email_a.visibility = View.VISIBLE
        }

        this.transmitable = false
        this.number = number
        this.startTime = System.currentTimeMillis()
        this.isShowing = true

        btn_send_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)

        btn_send_email_a.postDelayed({
            // 30초에 한번 이메일 전송 할 수 있도록 설정
            btn_send_email_a.setBackgroundResource(R.drawable.corner_round_primary)
            transmitable = true
        }, resending)

//        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다", Toast.LENGTH_SHORT).show()

        // 임시 코드
        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다 ${number.code}", Toast.LENGTH_SHORT)
            .show()
    }

    // 에러 보여주기
    override fun showError(msg: String) {
        hideLoading()

        Toast.makeText(this@EmailActivity, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        text_block_email_a.visibility = View.VISIBLE
        progressbar_wait_email_a.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        text_block_email_a.visibility = View.INVISIBLE
        progressbar_wait_email_a.visibility = View.INVISIBLE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_send_email_a -> {
                if (transmitable) { // 이메일 인증을 서버에 요청가능 할 때

                    showLoading()

                    presenter.sendEmail(
                        getString(R.string.baseurl),
                        edit_email_email_a.text.toString()
                    )
                } else { // 이메일 인증요청이 불가능 할 때
                    val elapsedTime = System.currentTimeMillis()
                    val remainingTime = resending / 1000 - (elapsedTime - startTime) / 1000
                    Toast.makeText(
                        applicationContext,
                        "${remainingTime}초 후 시도해주세요",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            R.id.btn_confirm_email_a -> {
                presenter.confirm(number, edit_number_email_a.text.toString())
            }
        }
    }
}
