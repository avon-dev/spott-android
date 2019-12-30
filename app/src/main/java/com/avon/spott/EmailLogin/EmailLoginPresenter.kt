package com.avon.spott.EmailLogin

import com.avon.spott.Utils.ValidatorModel

class EmailLoginPresenter(val emailLoginView: EmailLoginContract.View) :
    EmailLoginContract.Presenter {

    init {
        emailLoginView.presenter = this
    }

    override fun openMain() {
        emailLoginView.showMainUi()
    }

    override fun openFindPW() {
        emailLoginView.showFindPWUi()
    }

    override fun navigateUp() {
        emailLoginView.navigateUp()
    }

    override fun isEmail(email: String) {
        emailLoginView.isEmail(ValidatorModel.validEmail(email))
    }

    override fun isPassword(pw: String) {
        emailLoginView.isPassword(ValidatorModel.validPassword(pw))
    }
}