package com.avon.spott.Email

class EmailPresenter(val signUpView: EmailContract.View) : EmailContract.Presenter {

    init { signUpView.presenter = this }

    override fun openNumber() { signUpView.showNumberUi() }

    override fun navigateUp() { signUpView.navigateUp() }

    override fun send() { signUpView.sendEmail() }
}