package com.avon.spott.Password

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.User
import com.avon.spott.Email.INTENT_EXTRA_USER
import com.avon.spott.Nickname.NicknameActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.toolbar.*

class PasswordActivity : AppCompatActivity(), PasswordContract.View, View.OnClickListener {

    private lateinit var passwordPresenter: PasswordPresenter
    override lateinit var presenter: PasswordContract.Presenter

    private lateinit var user: User

    private var isPassword: Boolean = false
    private var isCheck: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        init()
    }

    fun init() {

        user = intent.getParcelableExtra(INTENT_EXTRA_USER)

        passwordPresenter = PasswordPresenter(this)

        text_title_toolbar.text = getString(R.string.pw)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_password_a.setOnClickListener(this)
        btn_confirm_password_a.isClickable = isCheck

        edit_password_a.addTextChangedListener {
            presenter.isPassword(it.toString())
            presenter.isPassword(it.toString(), edit_check_password_a.text.toString())
        }

        edit_check_password_a.addTextChangedListener {
            presenter.isPassword(edit_password_a.text.toString(), it.toString())
        }

    }

    override fun isPassword(warn: Boolean) {
        isPassword = warn
    }

    override fun isCheck(warn: Boolean) {
        isCheck = warn
        if (isPassword and isCheck) {
            btn_confirm_password_a.apply {
                isClickable = true
                setBackgroundResource(R.drawable.corner_round_primary)
            }
        } else {
            btn_confirm_password_a.apply {
                isClickable = false
                setBackgroundResource(R.drawable.corner_round_graybtn)
            }
        }
    }

    override fun showWarning() {
        if (isPassword)
            text_warnmessage_password_a.visibility = View.INVISIBLE
        else
            text_warnmessage_password_a.visibility = View.VISIBLE

        if (isCheck)
            text_warnmessage2_password_a.visibility = View.INVISIBLE
        else
            text_warnmessage2_password_a.visibility = View.VISIBLE
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showNicknameUi() {
        val intent = Intent(this@PasswordActivity, NicknameActivity::class.java)
        user.password = edit_check_password_a.text.toString()
        intent.putExtra(INTENT_EXTRA_USER, user)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_confirm_password_a -> {
                presenter.openNickname()
            }
        }
    }
}
