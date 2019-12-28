package com.avon.spott.Map

class MapPresenter (val mapView:MapContract.View) : MapContract.Presenter {
    init{ mapView.presenter = this}

    override fun openPhoto() { mapView.showPhotoUi() }
}