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
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.activity_email.*
import kotlinx.android.synthetic.main.toolbar.*


class EmailActivity : AppCompatActivity(), EmailContract.View, View.OnClickListener {

    private val TAG = "forEmailActivity"

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
    }

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


    override fun navigateUp() {
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) {
        isEmail = valid
        if (isEmail) text_warnmsg1_email_a.visibility = View.INVISIBLE
        else text_warnmsg1_email_a.visibility = View.VISIBLE
    }

    override fun isNumber(bool: Boolean) {
        if (bool)
            btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_primary)
        else
            btn_confirm_email_a.setBackgroundResource(R.drawable.corner_round_graybtn)
    }

    override fun showPasswordUi() {
        val intent = Intent(this, PasswordActivity::class.java)
        val user = User(edit_email_email_a.text.toString())
        intent.putExtra(INTENT_EXTRA_USER, user)
        startActivity(intent)
    }


    override fun getNumber(number: Number) {

        if (isFirst) {
            isFirst = false
            btn_send_email_a.text = getString(R.string.resend)

            edit_number_email_a.visibility = View.VISIBLE
            text_numbermessage_email_a.visibility = View.VISIBLE
            btn_confirm_email_a.visibility = View.VISIBLE
        }

        this.number = number
        startTime = System.currentTimeMillis()

        Toast.makeText(this@EmailActivity, "인증번호가 전송되었습니다", Toast.LENGTH_SHORT).show()
    }

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
