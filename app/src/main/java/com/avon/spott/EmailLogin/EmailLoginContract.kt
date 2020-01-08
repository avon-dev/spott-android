package com.avon.spott.EmailLogin

import com.avon.spott.BaseView
import com.avon.spott.Data.Token

interface EmailLoginContract {
    interface View : BaseView<Presenter> {
        fun showMainUi(token: Token)
        fun showFindPWUi()
        fun navigateUp()
        fun isEmail(valid:Boolean)
        fun isPassword(valid:Boolean)
        fun showError(error:String)
    }

    interface Presenter {
        fun openFindPW()
        fun navigateUp()
        fun isEmail(email:String)
        fun isPassword(pw:String)
        fun signIn(baseurl:String, email:String, password:String)
    }
}