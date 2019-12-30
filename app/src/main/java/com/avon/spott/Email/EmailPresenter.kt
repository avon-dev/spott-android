package com.avon.spott.Email

import com.avon.spott.Utils.ValidatorModel

class EmailPresenter(val signUpView: EmailContract.View) : EmailContract.Presenter {


    init { signUpView.presenter = this }

    override fun openPassword() { signUpView.showPasswordUi() }

    override fun navigateUp() { signUpView.navigateUp() }

    override fun isEmail(email: String) {
        signUpView.isEmail(ValidatorModel.validEmail(email))
    }

    override fun sendEmail() {
        signUpView.sendEmail()
    }
}