package com.avon.spott.Hashtag

import com.avon.spott.Data.HashtagPaging
import com.avon.spott.Data.HomeResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class HashtagPresenter(val hashtagView : HashtagContract.View):HashtagContract.Presenter {

    private val TAG = "HashtagPresenter"

    init{ hashtagView.presenter = this}

    override fun openPhoto(id: Int) {
        hashtagView.showPhotoUi(id)
    }

    override fun getPhotos(baseUrl: String, start: Int, hashtag:String) {


        val hashtagPaging = HashtagPaging(start, hashtagView.refreshTimeStamp, hashtag)
        Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/tag", Parser.toJson(hashtagPaging))

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<HomeResult>(string!!)

                if (start ==0){
                    hashtagView.clearAdapter()
                }else if(hashtagView.hasNext){
                    hashtagView.removePageLoading()
                }

                hashtagView.hasNext = result.pageable //페이지가 남아있는지 여부

                hashtagView.refreshTimeStamp = result.created_time //리프리쉬 타임 설정

                hashtagView.addItems(result.items) //아이템들 추가


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