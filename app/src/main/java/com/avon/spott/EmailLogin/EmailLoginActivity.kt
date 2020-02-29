package com.avon.spott.EmailLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Data.Token
import com.avon.spott.FindPW.FindPWActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*


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

        // 임시
        edit_username_emaillogin_a.setText("pms939@test.com")
        edit_password_emaillogin_a.setText("qwer1234!")


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

        // 얻은 access 토큰 값 뜯어보기 ,
//        val str = String(Base64.decode(token.access, 0))
//        val str2 = String(Base64.decode(token.refresh, 0))
//        logd(TAG, "decode\n${str}")
//        logd(TAG, "decode\n${str2}")

        Intent(applicationContext, MainActivity::class.java).let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }

        // 임시
//        Intent(applicationContext, EditMyInfoActivity::class.java).let {
//            startActivity(it)
//        }

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

                presenter.signIn(
                    getString(R.string.baseurl),
                    edit_username_emaillogin_a.text.toString(),
                    edit_password_emaillogin_a.text.toString()
                )
            }
        }
    }
}
