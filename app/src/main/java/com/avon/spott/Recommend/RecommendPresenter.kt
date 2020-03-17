package com.avon.spott.Recommend

import com.avon.spott.Data.HomeResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class RecommendPresenter (val recommendView:RecommendContract.View):RecommendContract.Presenter{

    private var TAG = "RecommendPresenter"

    init {
        recommendView.presenter = this
    }

    override fun openPhoto(photoId: Int) {
        recommendView.showPhotoUi(photoId)
    }

    override fun getPhotos(baseUrl: String) {
        Retrofit(baseUrl).get(App.prefs.token, "spott/posts-recommendation")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<HomeResult>(string!!)


                recommendView.clearAdapter()

                recommendView.addItems(result.items)


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
            })
    }
}