package com.avon.spott.Email

import com.avon.spott.BaseView
import com.avon.spott.Data.Number

interface EmailContract {
    interface View : BaseView<Presenter> {
        fun showPasswordUi()
        fun navigateUp()
        fun validEmail(valid: Boolean)
        fun validNumber(bool:Boolean)
        fun getNumber(number:Number)
        fun showError(msg:String)
    }

    interface Presenter {
        fun openPassword()
        fun navigateUp()
        fun isEmail(email: String)
        fun isNumber(number:String)
        fun sendEmail(baseUrl:String, email: String)
        fun confirm(number:Number, str:String)
    }
}