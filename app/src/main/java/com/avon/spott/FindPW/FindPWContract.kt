package com.avon.spott.FindPW

import com.avon.spott.BaseView
import com.avon.spott.Data.Number

interface FindPWContract {
    interface View : BaseView<Presenter> {
        fun navigateUp()
        fun isEmail(valid: Boolean)
        fun getNumber(number:Number)
    }

    interface Presenter {
        fun navigateUp()
        fun isEmail(email: String)
        fun sendEmail(email:String, baseUrl:String)
    }
}