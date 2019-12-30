package com.avon.spott.Nickname

import com.avon.spott.BaseView

interface NicknameContract {
    interface View:BaseView<Presenter> {
        fun navigateUp()
        fun showMainUi()
    }

    interface Presenter {
        fun navigateUp()
        fun openMain()
    }
}