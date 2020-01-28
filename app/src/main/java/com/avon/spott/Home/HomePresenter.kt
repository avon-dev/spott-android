package com.avon.spott.Home

import com.avon.spott.Data.Home
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class HomePresenter(val homeView:HomeContract.View) : HomeContract.Presenter {

    private val TAG = "HomePresenter"

    init{ homeView.presenter = this}
    override fun openPhoto(id:Int) { homeView.showPhotoUi(id)}

    override fun getPhotos(baseUrl:String, start:Int){ //수정 예정 사항, 여기에 start와 정렬 방식을 보내줘야함.
            Retrofit(baseUrl).get("/spott/posts", "change change" )
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val photos = Parser.fromJson<ArrayList<Home>>(string!!)

                //photos로 처리할 예정

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