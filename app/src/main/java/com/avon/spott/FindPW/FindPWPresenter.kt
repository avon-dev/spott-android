package com.avon.spott.FindPW

class FindPWPresenter(val findPWView: FindPWContract.View) : FindPWContract.Presenter {
    init { findPWView.presenter = this}

    override fun navigateUp() { findPWView.navigateUp() }
}