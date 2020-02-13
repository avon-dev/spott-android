package com.avon.spott.NewPassword

import com.avon.spott.Utils.Validator

class NewPasswordPresenter(val newPasswordView:NewPasswordContract.View) : NewPasswordContract.Presenter {

    init {
        newPasswordView.presenter = this
    }

    override fun navigateUp() {
        newPasswordView.navigateUp()
    }

    override fun isPassword(password: String) {
        newPasswordView.isPassword(Validator.validPassword(password))
    }

    override fun isCheck(password: String, checkpw: String) {
        newPasswordView.isCheck(checkpw.equals(password))
    }
}