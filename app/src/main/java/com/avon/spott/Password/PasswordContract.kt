package com.avon.spott.Password

import com.avon.spott.BaseView

interface PasswordContract {

    interface View: BaseView<Presenter> {
        fun navigateUp()
        fun showNicknameUi()
    }

    interface Presenter {
        fun navigateUp()
        fun openNickname()
    }
}