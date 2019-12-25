package com.avon.spott.FindPW

import com.avon.spott.BaseView

interface FindPWContract {
    interface View : BaseView<Presenter> {
        fun navigateUp()
    }

    interface Presenter {
        fun navigateUp()
    }
}