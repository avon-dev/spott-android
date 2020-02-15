package com.avon.spott.EditMyinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.ChangePassword.ChangePasswordActivity
import com.avon.spott.Data.UserInfo
import com.avon.spott.Login.LoginActivity
import com.avon.spott.R
import com.avon.spott.Utils.MySharedPreferences
import kotlinx.android.synthetic.main.activity_edit_my_info.*
import kotlinx.android.synthetic.main.toolbar.*

class EditMyInfoActivity : AppCompatActivity(), EditMyInfoContract.View, View.OnClickListener {

    override lateinit var presenter: EditMyInfoContract.Presenter
    private lateinit var editmyinfoPresenter: EditMyInfoPresenter

    private var editable:Boolean = false
    private lateinit var buffNickname: String
    private var validNickname:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_info)

        init()
    }


    private fun init() {
        editmyinfoPresenter = EditMyInfoPresenter(this)

        text_title_toolbar.setText(getString(R.string.title_editmyinfo))

        img_back_toolbar.setOnClickListener(this)
        frame_profile_editmyinfo_a.setOnClickListener(this)
        imgbtn_editnickname_editmyinfo_a.setOnClickListener(this)
        btn_changepw_editmyinfo_a.setOnClickListener(this)
        btn_withdrawal_editmyinfo_a.setOnClickListener(this)
        btn_signout_editmyinfo_a.setOnClickListener(this)

        val access = MySharedPreferences(applicationContext).prefs.getString("access", "")

        if (access != null) // Shared에 토큰값이 있을 때
            presenter.getUser(getString(R.string.baseurl), access)


        edit_nickname_editmyinfo_a.addTextChangedListener {
            presenter.isNickname(edit_nickname_editmyinfo_a.text.toString())
        }
    }

    override fun getUserInfo(userInfo: UserInfo) {
        buffNickname = userInfo.nickname.toString()

        btn_email_editmyinfo_a.setText(userInfo.email)

        edit_nickname_editmyinfo_a.setText(userInfo.nickname)
        if(userInfo.profile_image != null) { // 프로필 이미지 있으면 이미지 세팅하기
            // 이미지 세팅하고, 편집 글씨 지울까? 말까?
        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun validNickname(valid: Boolean) {
        validNickname = valid
    }

    override fun getNickname(result: Boolean) {
        if(!result) { // 닉네임 변경에 실패 했을 때
            edit_nickname_editmyinfo_a.setText(buffNickname) // 이전 닉네임으로 되돌리기
            Toast.makeText(applicationContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show()
        } else {
            buffNickname = edit_nickname_editmyinfo_a.text.toString()
        }
    }

    override fun withDrawl(result: Boolean) {
        if(result) {
            presenter.signOut(MySharedPreferences(this))
        } else {
            Toast.makeText(applicationContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show()
        }
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
                editable = !editable
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


                if(editable) { // 닉네임 변경하기
                    imgbtn_editnickname_editmyinfo_a.setImageResource(R.drawable.ic_done_black_24dp)

                    edit_nickname_editmyinfo_a.apply { // 수정 가능한 상태로 만들기
                        isFocusableInTouchMode = true
                        isFocusable = true
                        setSelection(edit_nickname_editmyinfo_a.length())
                    }

                    imm.showSoftInput(edit_nickname_editmyinfo_a, 0)
                }else { // 닉네임 변경 완료
                    imgbtn_editnickname_editmyinfo_a.setImageResource(R.drawable.baseline_edit_24)

                    edit_nickname_editmyinfo_a.apply { // 수정 불가능한 상태로 만들기
                        isClickable = false
                        isFocusable = false
                    }

                    imm.hideSoftInputFromWindow(edit_nickname_editmyinfo_a.windowToken, 0)

                    if(validNickname) { // 닉네임을 규칙에 맞게 작성했을 때에만 서버에 요청을 날린다.
                        val token =
                            MySharedPreferences(applicationContext).prefs.getString("access", "")

                        if (token != null && !buffNickname.equals(edit_nickname_editmyinfo_a.text.toString()))
                            presenter.changeNickname(getString(R.string.baseurl), token, edit_nickname_editmyinfo_a.text.toString())
                        else
                            Toast.makeText(applicationContext, "닉네임을 변경해주세요", Toast.LENGTH_SHORT).show()

                    } else { // 닉네임이 규칙에 맞지 않은경우
                        edit_nickname_editmyinfo_a.setText(buffNickname)
                        Toast.makeText(applicationContext, "${getString(R.string.hint_nickname)}", Toast.LENGTH_SHORT).show()
                    }
                }

            }

            R.id.btn_changepw_editmyinfo_a -> { // 비밀번호 변경
                // 현재 비밀번호 입력 후 새 비밀번호 입력 창을 띄운다
                Intent(applicationContext, ChangePasswordActivity::class.java).let{
                    startActivity(it)
                }
            }

            R.id.btn_withdrawal_editmyinfo_a -> { // 회원 탙퇴
                // 서버와 통신해서 데이터 없애고 로그아웃 로직
                val token = MySharedPreferences(applicationContext).prefs.getString("access", "")
                presenter.withDrawl(getString(R.string.baseurl), token)
            }

            R.id.btn_signout_editmyinfo_a -> { // 로그아웃
                presenter.signOut(MySharedPreferences(this))
            }
        }
    }
}
