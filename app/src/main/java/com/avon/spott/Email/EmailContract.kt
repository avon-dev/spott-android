package com.avon.spott.Email

import com.avon.spott.BaseView
import com.avon.spott.Data.Number

interface EmailContract {
    interface View : BaseView<Presenter> {
        fun showPasswordUi()
        fun navigateUp()
        fun isEmail(valid: Boolean)
        fun sendEmail(number:Number)
    }

    interface Presenter {
        fun openPassword()
        fun navigateUp()
        fun isEmail(email: String)
        fun sendEmail(email: String)
    }
}