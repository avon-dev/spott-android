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
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_nickname.*
import kotlinx.android.synthetic.main.toolbar.*

class NicknameActivity : AppCompatActivity(), NicknameContract.View, View.OnClickListener {

    override lateinit var presenter: NicknameContract.Presenter
    private lateinit var nicknamePresenter: NicknamePresenter

    val INTENT_EXTRA_USER = "user"
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
        if (enable) {
            btn_confirm_nickname_a.setBackgroundResource(R.drawable.corner_round_primary)
        } else {
            btn_confirm_nickname_a.setBackgroundResource(R.drawable.corner_round_graybtn)
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

    override fun showMainUi(token:Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        val intent = Intent(this@NicknameActivity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_confirm_nickname_a -> {
                if (edit_nickname_a.text.length > 3) {
                    user.nickname = edit_nickname_a.text.toString()
                    presenter.signUp(getString(R.string.baseurl), user)
                } else {
                    Toast.makeText(this@NicknameActivity, "닉네임을 4글자 이상 작성해주세요", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
