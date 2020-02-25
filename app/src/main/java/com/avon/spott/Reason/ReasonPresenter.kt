package com.avon.spott.Reason

import com.avon.spott.Data.NotiResult
import com.avon.spott.Data.ReasonResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class ReasonPresenter(val reasonView:ReasonContract.View):ReasonContract.Presenter{

    private var TAG = "ReasonPresenter"

    init {
        reasonView.presenter = this
    }

    override fun openGuideline() {
        reasonView.showGuidelineUi()
    }

    override fun getReason(baseUrl: String, notiId: Int) {
        Retrofit(baseUrl).get(App.prefs.token,"/spott/notice/"+notiId.toString(), "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                val result = Parser.fromJson<ReasonResult>(string!!)

                reasonView.setReason(result.image_url, result.caption)


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