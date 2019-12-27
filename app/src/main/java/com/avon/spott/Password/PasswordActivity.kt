package com.avon.spott.Password

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.avon.spott.Nickname.NicknameActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.toolbar.*

class PasswordActivity : AppCompatActivity(), PasswordContract.View, View.OnClickListener {

    lateinit var numberPresenter: PasswordPresenter
    override lateinit var presenter: PasswordContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        init()
    }

    fun init() {
        numberPresenter = PasswordPresenter(this)

        text_title_toolbar.text = getString(R.string.pw)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_password_a.setOnClickListener(this)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showNicknameUi() {
        val intent = Intent(this@PasswordActivity, NicknameActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp() }
            R.id.btn_confirm_password_a -> { presenter.openNickname() }
        }
    }
}
