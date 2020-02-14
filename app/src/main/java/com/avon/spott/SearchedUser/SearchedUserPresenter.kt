package com.avon.spott.SearchedUser

class SearchedUserPresenter (val searchedUserView : SearchedUserContract.View):SearchedUserContract.Presenter {

    private val TAG = "SearchedUserPresenter"

    init{ searchedUserView.presenter = this}
}