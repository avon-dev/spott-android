package com.avon.spott.Home

class HomePresenter(val homeView:HomeContract.View) : HomeContract.Presenter {
    init{ homeView.presenter = this}

    override fun openPhoto() { homeView.showPhotoUi() }
}