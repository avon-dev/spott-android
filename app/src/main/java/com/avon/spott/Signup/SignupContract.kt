package com.avon.spott.Signup

import com.avon.spott.BaseView

interface SignupContract {
    interface View:BaseView<Presenter> {
        fun showNicknameUi()
        fun navigateUp()
    }

    interface Presenter {
        fun openNickname()
        fun navigateUp()
    }
}