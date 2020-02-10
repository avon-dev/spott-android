package com.avon.spott.Main

import com.avon.spott.Utils.App
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class MainPresenter(val mainView:MainContract.View) : MainContract.Presenter {

    private val TAG = "MainPresenter"

    init{ mainView.presenter = this}

    override fun openCamera() { mainView.showCameraUi() }

    override fun navigateUp() { mainView.navigateUp() }

}