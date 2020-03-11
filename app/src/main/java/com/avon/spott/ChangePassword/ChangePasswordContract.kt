package com.avon.spott.ChangePassword

import com.avon.spott.BaseView

interface ChangePasswordContract {

    interface View:BaseView<Presenter> {
        fun navigateUp()
        fun checkPassword(check:Boolean)
        fun isPassword(isPassword:Boolean)
        fun isCheck(isCheck:Boolean)
        fun changedPassword(result:Boolean)

        fun showLoading()
        fun hideLoading()
    }

    interface Presenter {
        fun navigateUp() // 뒤로가기
        fun checkPassword(baseUrl:String, token:String, password:String) // 기존 패스워드 맞는지 확인하기
        fun isPassword(password:String) // 새 비밀번호 입력 창
        fun isCheck(password:String, checkpw:String) // 새 비밀번호 확인
        fun changePassword(baseUrl:String, token:String, password:String)
    }
}