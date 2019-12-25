package com.avon.spott.EmailLogin

import com.avon.spott.BaseView

interface EmailLoginContract {
    interface View:BaseView<Presenter> {
        fun showMainUi()
        fun showFindPWUi()
    }

    interface Presenter {
        fun openMain()
        fun openFindPW()
    }
}