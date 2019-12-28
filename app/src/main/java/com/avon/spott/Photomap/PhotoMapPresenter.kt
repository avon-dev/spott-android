package com.avon.spott.Photomap

class PhotoMapPresenter(val photoMapView:PhotoMapContract.View) : PhotoMapContract.Presenter {
    init{ photoMapView.presenter = this}
}