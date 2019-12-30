package com.avon.spott.Email

import com.avon.spott.BaseView

interface EmailContract {
    interface View : BaseView<Presenter> {
        fun showPasswordUi()
        fun navigateUp()
        fun isEmail(valid: Boolean)
        fun sendEmail()
    }

    interface Presenter {
        fun openPassword()
        fun navigateUp()
        fun isEmail(email: String)
        fun sendEmail()
    }
}