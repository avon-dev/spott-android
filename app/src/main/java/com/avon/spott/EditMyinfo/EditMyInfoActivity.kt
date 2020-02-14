package com.avon.spott.EditMyinfo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Login.LoginActivity
import com.avon.spott.R
import com.avon.spott.Utils.MySharedPreferences
import kotlinx.android.synthetic.main.activity_edit_my_info.*
import kotlinx.android.synthetic.main.toolbar.*

class EditMyInfoActivity : AppCompatActivity(), EditMyInfoContract.View, View.OnClickListener {

    override lateinit var presenter: EditMyInfoContract.Presenter
    private lateinit var editmyinfoPresenter: EditMyInfoPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_info)

        init()
    }

    private fun init() {
        editmyinfoPresenter = EditMyInfoPresenter(this)

        img_back_toolbar.setOnClickListener(this)
        frame_profile_editmyinfo_a.setOnClickListener(this)
        imgbtn_editnickname_editmyinfo_a.setOnClickListener(this)
        btn_changepw_editmyinfo_a.setOnClickListener(this)
        btn_withdrawal_editmyinfo_a.setOnClickListener(this)
        btn_signout_editmyinfo_a.setOnClickListener(this)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun loginActivity() { // 로그아웃
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }

            R.id.frame_profile_editmyinfo_a -> { // 프로필 이미지 편집

            }

            R.id.imgbtn_editnickname_editmyinfo_a -> { // 닉네임 수정
                imgbtn_editnickname_editmyinfo_a.setImageResource(R.drawable.ic_done_black_24dp)

            }

            R.id.btn_changepw_editmyinfo_a -> { // 비밀번호 변경
                // 이메일 인증 없는 newPassword 로직을 실행한다?
            }

            R.id.btn_withdrawal_editmyinfo_a -> { // 회원 탙퇴
                // 서버와 통신해서 데이터 없애고 로그아웃 로직
            }

            R.id.btn_signout_editmyinfo_a -> { // 로그아웃
                presenter.signOut(MySharedPreferences(this))
            }
        }
    }
}
