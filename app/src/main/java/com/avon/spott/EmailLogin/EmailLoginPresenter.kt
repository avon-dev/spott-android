package com.avon.spott.EmailLogin

class EmailLoginPresenter(val emailLoginView: EmailLoginContract.View) : EmailLoginContract.Presenter {

    init {
        emailLoginView.presenter = this
    }

    override fun openMain() {
        emailLoginView.showMainUi()
    }

    override fun openFindPW() {
        emailLoginView.showFindPWUi()
    }
}