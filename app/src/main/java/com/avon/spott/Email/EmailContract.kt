package com.avon.spott.Email

import com.avon.spott.BaseView

interface EmailContract {
    interface View:BaseView<Presenter> {
        fun showPasswordUi()
        fun navigateUp()
        fun sendEmail()
    }

    interface Presenter {
        fun openPassword()
        fun navigateUp()
        fun send()
    }
}