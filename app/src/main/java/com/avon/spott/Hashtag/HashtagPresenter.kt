package com.avon.spott.Hashtag

class HashtagPresenter(val hashtagView : HashtagContract.View):HashtagContract.Presenter {

    private val TAG = "HashtagPresenter"

    init{ hashtagView.presenter = this}
}