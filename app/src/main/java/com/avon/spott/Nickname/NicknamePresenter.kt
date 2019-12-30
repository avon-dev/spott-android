package com.avon.spott.Nickname

class NicknamePresenter(val nicknameView: NicknameContract.View) : NicknameContract.Presenter {
    init { nicknameView.presenter = this }

    override fun navigateUp() { nicknameView.navigateUp() }

    override fun openMain() { nicknameView.showMainUi() }
}