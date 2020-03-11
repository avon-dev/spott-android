package com.avon.spott.Email

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Camera.EMAIL_FIND_RESENDING_MILLS
import com.avon.spott.Data.Number
import com.avon.spott.Data.User
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import com.avon.spott.Utils.App
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
    private var isShowing = false;
    private var number: Number = Number(false)
    private lateinit var email:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        init()
    }

    // 초기화
    private fun init() {
        // 임시
        edit_email_email_a.setText("baek@seunghyun.com")

        signUpPresenter = EmailPresenter(this)

        text_title_toolbar.text = getString(R.string.email)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_email_a.setOnClickListener(this)
        btn_send_email_a.setOnClickListener(this)
        btn_confirm_email_a.setOnClickListener(this)

        edit_email_email_a.addTextChangedListener {
            presenter.isEmail(it.toString())
        }

//        edit_number_email_a.addTextChangedListener {
//            presenter.isNumber(it.toString())
//        }

//        text_block_email_a.setOnTouchListener { v, event -> true }
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

        email = edit_email_email_a.text.toString()

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

        // 임시 코드
//        Toast.makeText(this@EmailActivity, getString(R.string.send_authentication_number), Toast.LENGTH_SHORT).show()

        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다 ${number.code}", Toast.LENGTH_SHORT).show()
    }

    // 메세지 보여주기
    override fun showMessage(msgCode: Int) {

        val msg:String = when(msgCode) {
            App.SERVER_ERROR_400 -> {
                getString(R.string.error_400)
            }
            ERROR_DUPLICATION_EMAIL -> {
                getString(R.string.error_duplication_email)
            }
            CHECK_NUMBER -> {
                getString(R.string.error_check_number)
            }
            else -> {
                getString(R.string.error_retry)
            }
        }

        Toast.makeText(this@EmailActivity, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading() {
//        text_block_email_a.visibility = View.VISIBLE
        progressbar_wait_email_a.visibility = View.VISIBLE

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun hideLoading() {
//        text_block_email_a.visibility = View.GONE
        progressbar_wait_email_a.visibility = View.GONE

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_send_email_a -> {
                if (transmitable) { // 이메일 인증을 서버에 요청가능 할 때
                    presenter.sendEmail(
                        getString(R.string.baseurl),
                        edit_email_email_a.text.toString()
                    )
                } else { // 이메일 인증요청이 불가능 할 때
                    val elapsedTime = System.currentTimeMillis()
                    val remainingTime = resending / 1000 - (elapsedTime - startTime) / 1000
                    Toast.makeText(applicationContext, String.format(getString(R.string.wait_resending), remainingTime), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btn_confirm_email_a -> {
                presenter.confirm(number, email)
            }
        }
    }

    companion object ERROR {
        val ERROR_DUPLICATION_EMAIL = 1
        val CHECK_NUMBER = 2
    }
}
