package com.avon.spott.Nickname

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        init()
    }

    private fun init() {

        user = intent?.getParcelableExtra(INTENT_EXTRA_USER)!!

        nicknamePresenter = NicknamePresenter(this)

        text_title_toolbar.text = getString(R.string.nickname)
        edit_nickname_a.addTextChangedListener {
            presenter.isNickname(edit_nickname_a.text.toString())
        }

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_nickname_a.setOnClickListener(this)
    }

    override fun enableSignUp(enable: Boolean) {
        val btnConfirm = btn_confirm_nickname_a

        if (enable) {
            btnConfirm.apply {
                setBackgroundResource(R.drawable.corner_round_primary)
                isClickable = true
            }
        } else {
            btnConfirm.apply {
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
            presenter.getToken(getString(R.string.baseurl), user.email, user.password!!)
        } else {
            Toast.makeText(this@NicknameActivity, "이미 가입한 닉네임입니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getToken(token:Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        // 토큰 값 디코딩 가능한지 확인해보기 jwt는 인코딩이니 디코딩이 될 것이라 생각
        // 디코딩하면 남은 기간 체크
        // 남은 기간에 따른 토큰 재발급에 관련된 로직 필요

        showMainUi()
    }

    private fun showMainUi() {
        val intent = Intent(this@NicknameActivity, MainActivity::class.java)
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
                if (edit_nickname_a.text.length > 3) {
                    user.nickname = edit_nickname_a.text.toString()
                    presenter.signUp(getString(R.string.baseurl), user)
                } else {
                    Toast.makeText(this@NicknameActivity, "닉네임을 4글자 이상 작성해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
