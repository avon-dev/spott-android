package com.avon.spott.Scrap

class ScrapPresenter(val scrapView:ScrapContract.View) : ScrapContract.Presenter {
    init{ scrapView.presenter = this}

    override fun openPhoto() { scrapView.showPhotoUi() }
}