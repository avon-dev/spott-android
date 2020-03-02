package com.avon.spott.Nickname

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Data.User
import com.avon.spott.Email.INTENT_EXTRA_USER
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_nickname.*
import kotlinx.android.synthetic.main.toolbar.*

class NicknameActivity : AppCompatActivity(), NicknameContract.View, View.OnClickListener {

    override lateinit var presenter: NicknameContract.Presenter
    private lateinit var nicknamePresenter: NicknamePresenter

    private lateinit var emailUser: User
    private lateinit var socialUser: SocialUser

    private val EMAIL = 0
    private val SOCIAL = 1
    private var login:Int = -1

    private var enable = false

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
            val email = intent.extras.getString("email", "")
            socialUser = SocialUser(email)
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

    override fun signUp(result: Boolean) {
        if (result) {
            presenter.getToken(getString(R.string.baseurl), emailUser.email, emailUser.password!!)
        } else {
            Toast.makeText(this@NicknameActivity, getString(R.string.toast_duplication_nickname), Toast.LENGTH_SHORT).show()
        }
    }

    override fun getToken(token:Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        showMainUi()
    }

    private fun showMainUi() {
        val intent = Intent(this@NicknameActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            // 뒤로가기
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }

            // 가입하기
            R.id.btn_confirm_nickname_a -> {
                if (enable) {
                    if(login == EMAIL) {
                        emailUser.nickname = edit_nickname_a.text.toString()
                        presenter.signUp(getString(R.string.baseurl), emailUser)
                    } else if (login == SOCIAL) {
                        socialUser.nickname = edit_nickname_a.text.toString()
//                        presenter.signUp(getString(R.string.baseurl), "socialurl", socialUser)
                        Toast.makeText(this@NicknameActivity, "소셜 회원 가입", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@NicknameActivity, "닉네임을 4글자 이상 작성해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
