package com.avon.spott.EmailLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Data.Token
import com.avon.spott.FindPW.FindPWActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.App
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*
import java.security.cert.Certificate


class EmailLoginActivity : AppCompatActivity(), EmailLoginContract.View, View.OnClickListener {

    override lateinit var presenter: EmailLoginContract.Presenter
    private lateinit var emailLoginPresenter: EmailLoginPresenter

    private val TAG = "EmailLoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        init()
    }

    private fun init() {

        emailLoginPresenter = EmailLoginPresenter(this)

        text_title_toolbar.text = getString(R.string.text_title_emaillogin)

        img_back_toolbar.setOnClickListener(this)
        text_findpw_emaillogin_a.setOnClickListener(this)
        btn_login_emaillogin_a.setOnClickListener(this)
    }

    override fun showMainUi(token: Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        Intent(applicationContext, MainActivity::class.java).let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    override fun showFindPWUi() {
        val intent = Intent(this@EmailLoginActivity, FindPWActivity::class.java)
        startActivity(intent)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showError(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }

            R.id.text_findpw_emaillogin_a -> {
                presenter.openFindPW()
            }

            R.id.btn_login_emaillogin_a -> { // 로그인 버튼

                if (edit_username_emaillogin_a.text.toString().equals("")) {
                    showError("이메일을 입력해주세요")
                    return
                }

                if (edit_password_emaillogin_a.text.toString().equals("")) {
                    showError("비밀번호를 입력해주세요")
                    return
                }

                window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                presenter.getPublicKey(getString(R.string.baseurl),"/spott/publickey")
            }
        }
    }

    override fun showMessage(msgCode: Int) {
        val msg:String
        when (msgCode) {
            App.ERROR_PUBLICKEY -> { msg = getString(R.string.error_retry) }
            App.SERVER_ERROR_400 -> { msg = getString(R.string.error_400) }
            App.SERVER_ERROR_404 -> { msg = getString(R.string.error_404) }
            App.SERVER_ERROR_500 -> { msg = getString(R.string.error_500) }
            else -> { msg = getString(R.string.error_retry) }
        }
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
    override fun getPublicKey(certificate: Certificate) {
        presenter.signIn(
            getString(R.string.baseurl),
            edit_username_emaillogin_a.text.toString(),
            edit_password_emaillogin_a.text.toString(),
            certificate
        )
    }
}
