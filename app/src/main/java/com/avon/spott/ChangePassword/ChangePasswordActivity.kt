package com.avon.spott.ChangePassword

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import com.avon.spott.Utils.MySharedPreferences
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.android.synthetic.main.toolbar.*

class ChangePasswordActivity : AppCompatActivity(), ChangePasswordContract.View,
    View.OnClickListener {

    override lateinit var presenter: ChangePasswordContract.Presenter
    private lateinit var changePasswordPresenter: ChangePasswordPresenter
    private var originPassword = false

    private var isPassword = false
    private var isCheck = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        init()
    }

    private fun init() {
        // 임시
//        edit_origin_changepassword_a.setText("seunghyun1!")

        text_title_toolbar.text = getString(R.string.changepw)

        changePasswordPresenter = ChangePasswordPresenter(this)

        img_back_toolbar.setOnClickListener(this)
        btn_check_changepassword_a.setOnClickListener(this)
        btn_confirm_changepassword_a.setOnClickListener(this)

        // 새 비밀번호 입력
        edit_changepassword_a.addTextChangedListener {
            // email check 하기
            presenter.isPassword(edit_changepassword_a.text.toString())
        }

        // 새 비밀번호 입력 확인
        edit_check_changepassword_a.addTextChangedListener {
            // email double check
            presenter.isCheck(
                edit_changepassword_a.text.toString(),
                edit_check_changepassword_a.text.toString()
            )
        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun checkPassword(check: Boolean) {
        originPassword = check
        if (originPassword) // 비밀번호 확인
            Toast.makeText(applicationContext, getString(R.string.checked), Toast.LENGTH_SHORT).show()
        else // 비밀번호 확인 실패
            Toast.makeText(applicationContext, getString(R.string.error_wrong_password), Toast.LENGTH_SHORT).show()
    }

    override fun isPassword(isPassword: Boolean) { // 비밀번호를 규칙에 맞게 작성했는지
        this.isPassword = isPassword
        if(isPassword) {
           text_warnmessage_changepassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage_changepassword_a.visibility = View.VISIBLE
        }
    }

    override fun isCheck(isCheck: Boolean) { // 비밀번호를 같게 입력했는지?
        this.isCheck = isCheck
        if(isCheck) {
            text_warnmessage2_changepassword_a.visibility = View.INVISIBLE
        } else {
            text_warnmessage2_changepassword_a.visibility = View.VISIBLE
        }

    }

    override fun changedPassword(result: Boolean) {
        if(result) {
            Toast.makeText(applicationContext, getString(R.string.toast_success_changepw), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(applicationContext, getString(R.string.toast_fail_changepw), Toast.LENGTH_SHORT).show()
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }

            R.id.btn_check_changepassword_a -> { // 기존 비밀번호 확인하기
                val token = MySharedPreferences(applicationContext).prefs.getString("access", "")

                if (token!= null && !token.equals(""))
                    presenter.checkPassword(getString(R.string.baseurl),token, edit_origin_changepassword_a.text.toString())
            }

            R.id.btn_confirm_changepassword_a -> { // 새 비밀번호로 변경하기
                // 기존 비밀번호가 맞는지 확인 끝난 후 변경 가능
                if(originPassword) {
                    if(isPassword && isCheck) {
                        val token = MySharedPreferences(applicationContext).prefs.getString("access", "")

                        if(token!=null && !token.equals(""))
                            presenter.changePassword(getString(R.string.baseurl), token, edit_changepassword_a.text.toString())
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.toast_check_changepassword), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, getString(R.string.toast_origin_changepassword), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
