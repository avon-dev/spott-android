package com.avon.spott.EmailLogin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.Token
import com.avon.spott.FindPW.FindPWActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*


class EmailLoginActivity : AppCompatActivity(), EmailLoginContract.View, View.OnClickListener {

    override lateinit var presenter: EmailLoginContract.Presenter
    private lateinit var emailLoginPresenter: EmailLoginPresenter

    private var isEmail: Boolean = false
    private var isPassword: Boolean = false

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

        edit_username_emaillogin_a.addTextChangedListener {
            presenter.isEmail(it.toString())
        }

        edit_password_emaillogin_a.addTextChangedListener {
            presenter.isPassword(it.toString())
        }
    }

    override fun showMainUi(token: Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        val intent = Intent(this@EmailLoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun showFindPWUi() {
        val intent = Intent(this@EmailLoginActivity, FindPWActivity::class.java)
        startActivity(intent)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun isEmail(valid: Boolean) {
        isEmail = valid
        next()
    }

    override fun isPassword(valid: Boolean) {
        isPassword = valid
        next()
    }

    fun next() {
        if (isEmail && isPassword) {
            btn_login_emaillogin_a.isClickable = true
            btn_login_emaillogin_a.setBackgroundResource(R.drawable.corner_round_primary)
        } else {
            btn_login_emaillogin_a.isClickable = false
            btn_login_emaillogin_a.setBackgroundResource(R.drawable.corner_round_graybtn)
        }
    }

    override fun showError(error: String) {
        Toast.makeText(this@EmailLoginActivity, error, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }

            R.id.text_findpw_emaillogin_a -> {
                presenter.openFindPW()
            }

            R.id.btn_login_emaillogin_a -> {
                presenter.signIn(
                    getString(R.string.baseurl),
                    edit_username_emaillogin_a.text.toString(),
                    edit_password_emaillogin_a.text.toString()
                )
            }
        }
    }
}
