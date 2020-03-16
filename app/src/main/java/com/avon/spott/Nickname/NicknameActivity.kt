package com.avon.spott.Nickname

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Data.User
import com.avon.spott.Email.INTENT_EXTRA_USER
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.App
import kotlinx.android.synthetic.main.activity_nickname.*
import kotlinx.android.synthetic.main.toolbar.*
import java.security.cert.Certificate

class NicknameActivity : AppCompatActivity(), NicknameContract.View, View.OnClickListener {

    override lateinit var presenter: NicknameContract.Presenter
    private lateinit var nicknamePresenter: NicknamePresenter

    private lateinit var emailUser: User
    private lateinit var socialUser: SocialUser

    private val EMAIL = 0
    private val SOCIAL = 1

    private var login:Int = -1

    private var enable = false

    companion object {
        val ERROR_DUPLICATION = 4000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        init()
    }

    private fun init() {

        login = intent.extras.getInt("login", EMAIL)

        if(login == EMAIL) { // 이메일 로그인일 때
            emailUser = intent?.getParcelableExtra(INTENT_EXTRA_USER)!!
        } else if (login == SOCIAL) { // 소셜 로그인일 때
            socialUser= intent.getParcelableExtra(INTENT_EXTRA_USER)
        }

        nicknamePresenter = NicknamePresenter(this)

        text_title_toolbar.text = getString(R.string.nickname)
        edit_nickname_a.addTextChangedListener {
            presenter.isNickname(edit_nickname_a.text.toString())
        }

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_nickname_a.setOnClickListener(this)
    }

    override fun enableSignUp(enable: Boolean) {
        this.enable = enable

        if (enable) {
            btn_confirm_nickname_a.apply {
                setBackgroundResource(R.drawable.corner_round_primary)
                isClickable = true
            }
        } else {
            btn_confirm_nickname_a.apply {
                setBackgroundResource(R.drawable.corner_round_graybtn)
                isClickable = false
            }
        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun socialSignup() {
        presenter.getToken(getString(R.string.baseurl), "/spot/token", socialUser)
    }

    private fun showMainUi() {
        val intent = Intent(this@NicknameActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun showLoading() {
        progressbar_wait_nickname_a.visibility = View.VISIBLE

        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun hideLoading() {
        progressbar_wait_nickname_a.visibility = View.GONE

        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    override fun showMessage(msgCode: Int) {
        val msg:String
        when (msgCode) {
            App.ERROR_PUBLICKEY -> { msg = getString(R.string.error_retry) }
            App.SERVER_ERROR_400 -> { msg = getString(R.string.error_400) }
            App.SERVER_ERROR_404 -> { msg = getString(R.string.error_404) }
            App.SERVER_ERROR_500 -> { msg = getString(R.string.error_500) }
            ERROR_DUPLICATION -> { msg = getString(R.string.error_duplication_nickname)}
            else -> { msg = getString(R.string.error_retry) }
        }
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    override fun getPublicKey(certificate: Certificate) {
        emailUser.nickname = edit_nickname_a.text.toString()
        presenter.signUp(getString(R.string.baseurl), emailUser, certificate)
    }

    override fun signUp(certificate: Certificate) {
        presenter.getToken(getString(R.string.baseurl), emailUser.email, emailUser.password!!, emailUser.user_type, certificate)
    }

    override fun getToken(token:Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        showMainUi()
    }

    override fun addClickListener() { btn_confirm_nickname_a.setOnClickListener(this) }

    override fun onClick(v: View?) {
        when (v?.id) {

            // 뒤로가기
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }

            // 가입하기
            R.id.btn_confirm_nickname_a -> {
                if (enable) {
                    btn_confirm_nickname_a.setOnClickListener(null)
                    if(login == EMAIL) {
                        presenter.getPublicKey(getString(R.string.baseurl), "/spott/publickey")
                    } else if (login == SOCIAL) {
                        socialUser.nickname = edit_nickname_a.text.toString()
                            presenter.signUp(getString(R.string.baseurl), "/spott/social-account", socialUser)
                    }
                } else {
                    Toast.makeText(this@NicknameActivity, getString(R.string.error_invalid_nickname), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
