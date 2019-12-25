package com.avon.spott.Signup

import com.avon.spott.BaseView

class SignupPresenter(val signUpView: SignupContract.View) : SignupContract.Presenter {

    init { signUpView.presenter = this }

    override fun openNickname() { signUpView.showNicknameUi() }

    override fun navigateUp() { signUpView.navigateUp() }
}