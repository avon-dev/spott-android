package com.avon.spott.FindPlace

import com.avon.spott.BaseView

interface FindPlaceContract {
    interface View: BaseView<Presenter> {
        fun navigateUp()
    }

    interface Presenter{
        fun navigateUp()
    }
}