package com.avon.spott.FindPW

import com.avon.spott.BaseView

interface FindPWContract {
    interface View : BaseView<Presenter> {
        fun navigateUp()
        fun isEmail(valid: Boolean)
    }

    interface Presenter {
        fun navigateUp()
        fun isEmail(email: String)
    }
}