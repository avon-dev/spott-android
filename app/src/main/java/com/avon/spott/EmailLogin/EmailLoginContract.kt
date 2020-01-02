package com.avon.spott.EmailLogin

import com.avon.spott.BaseView

interface EmailLoginContract {
    interface View : BaseView<Presenter> {
        fun showMainUi()
        fun showFindPWUi()
        fun navigateUp()
        fun isEmail(valid:Boolean)
        fun isPassword(valid:Boolean)
        fun showError(error:String)
    }

    interface Presenter {
        fun openMain()
        fun openFindPW()
        fun navigateUp()
        fun isEmail(email:String)
        fun isPassword(pw:String)
        fun signIn(email:String, password:String)
        fun authentication()
    }
}