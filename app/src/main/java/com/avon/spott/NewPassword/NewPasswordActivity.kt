package com.avon.spott.NewPassword

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.FindPW.FIND_PW
import com.avon.spott.Login.LoginActivity
import com.avon.spott.R
import com.avon.spott.Utils.App
import kotlinx.android.synthetic.main.activity_new_password.*
import kotlinx.android.synthetic.main.toolbar.*

class NewPasswordActivity : AppCompatActivity(), NewPasswordContract.View, View.OnClickListener {

    override lateinit var presenter: NewPasswordContract.Presenter
    private lateinit var newPasswordPresenter:NewPasswordPresenter

    private val email:String by lazy { intent.getStringExtra(FIND_PW) }

    private var isPassword:Boolean = false
    private var isCheck:Boolean = false

    private val SUCCESS_CHANGE_PASSWORD = 1
    private val ERROR_CHECK_PASSWORD = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        init()
    }

    private fun init() {
        newPasswordPresenter = NewPasswordPresenter(this)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_newpassword_a.setOnClickListener(this)

        text_title_toolbar.text = getString(R.string.newpw)

        edit_newpassword_a.addTextChangedListener { // email check 하기
            presenter.isPassword(edit_newpassword_a.text.toString())
        }

        edit_check_newpassword_a.addTextChangedListener { // email double check
            presenter.isCheck(edit_newpassword_a.text.toString(), edit_check_newpassword_a.text.toString())
        }

    }


    override fun navigateUp() {
        onBackPressed()
    }

    override fun isPassword(isPassword: Boolean) {
        this.isPassword = isPassword
        presenter.isCheck(edit_newpassword_a.text.toString(), edit_check_newpassword_a.text.toString())
    }

    override fun isCheck(isCheck: Boolean) {
        this.isCheck = isCheck
//        showWaringMessage()
    }

    override fun showMessage(code: Int) {
        when (code) {
            App.SERVER_ERROR_400 -> {
                Toast.makeText(applicationContext, getString(R.string.error_400), Toast.LENGTH_SHORT).show()
            }
            App.SERVER_ERROR_404 -> {
                Toast.makeText(applicationContext, getString(R.string.error_404), Toast.LENGTH_SHORT).show()
            }
            App.SERVER_ERROR_500 -> {
                Toast.makeText(applicationContext, getString(R.string.error_500), Toast.LENGTH_SHORT).show()
            }
            App.ERROR_ERTRY -> {
                Toast.makeText(applicationContext, getString(R.string.error_retry), Toast.LENGTH_SHORT).show()
            }
            ERROR_CHECK_PASSWORD -> {
                Toast.makeText(applicationContext, getString(R.string.error_check_password), Toast.LENGTH_SHORT).show()
            }
            SUCCESS_CHANGE_PASSWORD -> {
                Toast.makeText(applicationContext, getString(R.string.success_changed_password), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun showWarning() {
        if(isPassword) {
            text_warnmessage_newpassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage_newpassword_a.visibility = View.VISIBLE
        }

        if(isCheck) {
            text_warnmessage2_newpassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage2_newpassword_a.visibility = View.VISIBLE
        }
    }

    override fun fixResult(result: Boolean) {
        if(result) { // 변경되었을 때
            Intent(applicationContext, LoginActivity::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
            showMessage(SUCCESS_CHANGE_PASSWORD)
        }
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }
            R.id.btn_confirm_newpassword_a -> {
                // 비밀번호 변경완료 버튼 클릭
                // 서버에 바뀐 비밀번호 알려주기
                // 이메일 로그인 화면으로 이동하는게 나을지, 아예 처음 로그인 화면이 나을지
                if(isPassword and isCheck)
                    presenter.fixPassword(getString(R.string.baseurl), email, edit_newpassword_a.text.toString())
                else {
                    showMessage(ERROR_CHECK_PASSWORD)
                }
            }
        }
    }
}
