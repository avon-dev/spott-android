package com.avon.spott.Password

import com.avon.spott.BaseView

interface PasswordContract {

    interface View: BaseView<Presenter> {
        fun navigateUp()
        fun showNicknameUi()
        fun isPassword(warn:Boolean)
        fun isCheck(warn:Boolean)
        fun showWarning()
    }

    interface Presenter {
        fun navigateUp()
        fun openNickname()
        fun isPassword(password:String)
        fun isPassword(password:String, check:String)
    }
}