package com.avon.spott.EmailLogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.Token
import com.avon.spott.FindPW.FindPWActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*

class EmailLoginActivity : AppCompatActivity(), EmailLoginContract.View, View.OnClickListener {

    override lateinit var presenter: EmailLoginContract.Presenter
    private lateinit var emailLoginPresenter: EmailLoginPresenter

    // 유효성 검사 객체
//    private val validator: Validator by lazy { Validator.getInstance() }

    private var isEmail: Boolean = false
    private var isPassword: Boolean = false

    // 서버에서 받아온 토큰 값
    private var token: Token? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        init() // 초기화
    }

    override fun onDestroy() {
        CompositeDisposable().dispose()
        super.onDestroy()
    }

    // 초기화
    private fun init() {
        // 프레젠터 생성
        emailLoginPresenter = EmailLoginPresenter(this)

        // 타이틀 설정
        text_title_toolbar.text = getString(R.string.text_title_emaillogin)

        // 버튼 클릭 리스너
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

    // 메인으로 이동
    override fun showMainUi() {
        val intent = Intent(this@EmailLoginActivity, MainActivity::class.java)
        startActivity(intent)
    }

    // 비밀번호 찾기로 이동
    override fun showFindPWUi() {
        val intent = Intent(this@EmailLoginActivity, FindPWActivity::class.java)
        startActivity(intent)
    }

    // 뒤로가기
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

    override fun onClick(v: View?) {
        when (v?.id) {
            // 뒤로가기
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.text_findpw_emaillogin_a -> {
                presenter.openFindPW()
            }
            R.id.btn_login_emaillogin_a -> {
                presenter.openMain()
            }
        }
    }
}
