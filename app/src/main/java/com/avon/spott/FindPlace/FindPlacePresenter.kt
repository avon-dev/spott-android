package com.avon.spott.FindPlace

class FindPlacePresenter(val findPlaceView:FindPlaceContract.View):FindPlaceContract.Presenter {

    private val TAG = "FindPlacePresenter"

    init { findPlaceView.presenter = this }

    override fun navigateUp() {
        findPlaceView.navigateUp()
    }

}