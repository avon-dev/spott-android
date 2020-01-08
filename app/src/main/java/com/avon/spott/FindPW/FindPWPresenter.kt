package com.avon.spott.FindPW

import com.avon.spott.Utils.Validator

class FindPWPresenter(val findPWView: FindPWContract.View) : FindPWContract.Presenter {
    init { findPWView.presenter = this}

    override fun navigateUp() { findPWView.navigateUp() }

    override fun isEmail(email: String) { findPWView.isEmail(Validator.validEmail(email)) }
}