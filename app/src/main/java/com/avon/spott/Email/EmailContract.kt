package com.avon.spott.Email

import com.avon.spott.BaseView

interface EmailContract {
    interface View:BaseView<Presenter> {
        fun showNumberUi()
        fun navigateUp()
        fun sendEmail()
    }

    interface Presenter {
        fun openNumber()
        fun navigateUp()
        fun send()
    }
}