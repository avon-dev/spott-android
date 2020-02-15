package com.avon.spott.NewPassword

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_new_password.*
import kotlinx.android.synthetic.main.toolbar.*

class NewPasswordActivity : AppCompatActivity(), NewPasswordContract.View, View.OnClickListener {

    override lateinit var presenter: NewPasswordContract.Presenter
    private lateinit var newPasswordPresenter:NewPasswordPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_password)

        init()
    }

    private fun init() {
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
        if(isPassword) {
            text_warnmessage_newpassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage_newpassword_a.visibility = View.VISIBLE
        }
    }

    override fun isCheck(isCheck: Boolean) {
        if(isCheck) {
            text_warnmessage2_newpassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage2_newpassword_a.visibility = View.VISIBLE
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
            }
        }
    }
}
