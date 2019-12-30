package com.avon.spott.Password

class PasswordPresenter (val numberView:PasswordContract.View) : PasswordContract.Presenter {
    init {
        numberView.presenter = this
    }

    override fun navigateUp() {
        numberView.navigateUp()
    }

    override fun openNickname() {
        numberView.showNickname()
    }
}