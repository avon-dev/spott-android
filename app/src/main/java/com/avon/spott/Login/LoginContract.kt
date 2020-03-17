package com.avon.spott.Login

import com.avon.spott.BaseView
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token

interface LoginContract {
    interface View:BaseView<Presenter> {
        fun showEmailLoginUi() // EmailLoginActivity 이동
        fun showSignupUi() // SignupActivity 이동
        fun showMainUi(token: Token)
        fun isPhopoUser()
        fun notPhopoUser()
        fun showMessage(msgCode:Int)
    }
    interface Presenter {
        fun openEmailLogin() // EmailLogin 이동
        fun openSignup() // SignupActivity 이동
        fun isPhopoUser(baseUrl:String, url:String, socialUser: SocialUser)
        fun getToken(baseUrl: String, url:String, socialUser: SocialUser)
    }
}