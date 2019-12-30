package com.avon.spott.Main

class MainPresenter(val mainView:MainContract.View) : MainContract.Presenter {

    init{ mainView.presenter = this}

    override fun openCamera() { mainView.showCameraUi() }

    override fun navigateUp() { mainView.navigateUp() }
}