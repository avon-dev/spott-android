package com.avon.spott.Notice

import com.avon.spott.Data.Notice
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException


class NoticePresenter (val noticeView: NoticeContract.View): NoticeContract.Presenter {

    private val TAG = " NoticePresenter"

    init{noticeView.presenter = this}

    override fun navigateUp() {
        noticeView.navigateUp()
    }

    override fun openNoticeDetail(restUrl: String) {
        noticeView.showNoticeDetailUi(restUrl)
    }

    override fun getNotice(baseUrl: String) {
        Retrofit(baseUrl).get(App.prefs.token, "/spott/app-notices")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<ArrayList<Notice>>(string!!)

                noticeView.clearAdapter()

                noticeView.addItems(result)

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