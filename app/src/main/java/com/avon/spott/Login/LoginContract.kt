package com.avon.spott.Login

import com.avon.spott.BaseView

interface LoginContract {
    interface View:BaseView<Presenter> {
        fun showEmailLoginUi() // EmailLoginActivity 이동
        fun showSignupUi() // SignupActivity 이동
    }
    interface Presenter {
        fun openEmailLogin() // EmailLogin 이동
        fun openSignup() // SignupActivity 이도
    }
}