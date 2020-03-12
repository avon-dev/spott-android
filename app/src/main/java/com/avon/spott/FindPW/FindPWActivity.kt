package com.avon.spott.FindPW

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Camera.EMAIL_FIND_RESENDING_MILLS
import com.avon.spott.Data.Number
import com.avon.spott.NewPassword.NewPasswordActivity
import com.avon.spott.R
import com.avon.spott.Utils.App
import kotlinx.android.synthetic.main.activity_find_pw.*
import kotlinx.android.synthetic.main.toolbar.*

const val FIND_PW = "find_pw"
class FindPWActivity : AppCompatActivity(), FindPWContract.View, View.OnClickListener {

    override lateinit var presenter: FindPWContract.Presenter
    private lateinit var findPWPresenter: FindPWPresenter
    private lateinit var number: Number

    private val resending = EMAIL_FIND_RESENDING_MILLS

    private var transmitable = true // 이메일을 보낼 수 있는 상태인지?

    private var startTime: Long = 0L

    private lateinit var email:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pw)

        init()
    }

    private fun init() {
        findPWPresenter = FindPWPresenter(this)

        text_title_toolbar.text = getString(R.string.findpw)

        img_back_toolbar.setOnClickListener(this)
        btn_send_findpw_a.setOnClickListener(this)
        btn_confirm_findpw_a.setOnClickListener(this)

        edit_email_findpw_a.addTextChangedListener {
            presenter.isEmail(it.toString())
        }

    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) {
        if (valid) {// 이메일이 맞을 때
            text_warnmsg_findpw_a.visibility = View.INVISIBLE
            if(transmitable)
                btn_send_findpw_a.setBackgroundResource(R.drawable.corner_round_primary)
        } else { // 이메일이 아닐 때
            text_warnmsg_findpw_a.visibility = View.VISIBLE
            if(transmitable)
                btn_send_findpw_a.setBackgroundResource(R.drawable.corner_round_graybtn)
        }
    }

    override fun getNumber(number: Number) {
        email = edit_email_findpw_a.text.toString()

        this.transmitable = false
        this.number = number
        this.startTime = System.currentTimeMillis()

        edit_number_findpw_a.visibility = View.VISIBLE
        text_numbermessage_findpw_a.visibility = View.VISIBLE
        btn_confirm_findpw_a.visibility = View.VISIBLE

        btn_send_findpw_a.setBackgroundResource(R.drawable.corner_round_graybtn)

//        Toast.makeText(this@FindPWActivity, getString(R.string.send_authentication_number), Toast.LENGTH_SHORT).show()

        // 임시
        Toast.makeText(this@FindPWActivity, "인증번호가 전송되었습니다 ${number.code}", Toast.LENGTH_LONG).show()

        btn_send_findpw_a.postDelayed({ // 30초에 한번 보낼 수 있도록 설정
            btn_send_findpw_a.setBackgroundResource(R.drawable.corner_round_primary)
            transmitable = true
        }, resending)
    }

    override fun showMessage(msgCode: Int) {

        val msg:String = when(msgCode) {
            App.SERVER_ERROR_400 -> {
                getString(R.string.error_400)
            }
            ERROR_NON_EMAIL -> {
                getString(R.string.error_none_email)
            }
            else -> {
                getString(R.string.error_retry)
            }
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    }

    override fun showLoading() {
//        text_block_findpw_a.visibility = View.VISIBLE
        progress_findpw_a.visibility = View.VISIBLE

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun hideLoading() {
//        text_block_findpw_a.visibility = View.GONE
        progress_findpw_a.visibility = View.GONE

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_send_findpw_a -> {
                if (transmitable) {
                    presenter.sendEmail(
                        edit_email_findpw_a.text.toString(),
                        getString(R.string.baseurl)
                    )
                } else {
                    val elapsedTime = System.currentTimeMillis()
                    val remainingTime = resending / 1000 - (elapsedTime - startTime) / 1000
                    Toast.makeText(applicationContext, String.format(getString(R.string.wait_resending), remainingTime), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.btn_confirm_findpw_a -> {
                if(edit_number_findpw_a.text.toString().equals(number.code)) {
                    Intent(this@FindPWActivity, NewPasswordActivity::class.java).let {
                        it.putExtra(FIND_PW, email)
                        startActivity(it)
                    }
                } else {
                    Toast.makeText(
                        this@FindPWActivity,
                        getString(R.string.toast_wrong_number_findpw_a),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        val ERROR_NON_EMAIL = 1
    }

}
