package com.avon.spott.EditMyinfo

import com.avon.spott.Utils.MySharedPreferences

class EditMyInfoPresenter(val editMyInfoView:EditMyInfoContract.View) : EditMyInfoContract.Presenter {

    private val TAG = "EditMyInfoPresenter"

    init {
        editMyInfoView.presenter = this
    }

    override fun navigateUp() {
        editMyInfoView.navigateUp()
    }

    override fun signOut(pref:MySharedPreferences) {
        EditMyInfoModel.deleteToken(pref)
        editMyInfoView.loginActivity()
    }
}