package com.avon.spott.Password

import com.avon.spott.Utils.Validator

class PasswordPresenter(val numberView: PasswordContract.View) : PasswordContract.Presenter {
    init {
        numberView.presenter = this
    }

    override fun navigateUp() {
        numberView.navigateUp()
    }

    override fun openNickname() {
        numberView.showNicknameUi()
    }

    override fun isPassword(password: String) {
        numberView.isPassword(Validator.validPassword(password))
    }

    override fun isPassword(password: String, check: String) {
        numberView.isCheck(check.equals(password))
        numberView.showWarning()
    }
}