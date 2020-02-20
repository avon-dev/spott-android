package com.avon.spott.Home

import com.avon.spott.Data.HomeItem
import com.avon.spott.Data.HomePaging
import com.avon.spott.Data.HomeResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class HomePresenter(val homeView:HomeContract.View) : HomeContract.Presenter {

    private val TAG = "HomePresenter"

    init{ homeView.presenter = this}
    override fun openPhoto(id:Int) { homeView.showPhotoUi(id)}

    override fun openSearch(){homeView.showSearchUi()}

    override fun getToken(baseUrl: String, start:Int) {
        if(App.prefs.temporary_token!=""){
            logd(TAG, "토큰 있음")
            getPhotos(baseUrl, start)
        }else{
            logd(TAG, "토큰 없음")
            Retrofit(baseUrl).postNonHeader( "/spott/home/token","")
                .subscribe({ response ->
                    logd(TAG, response.body())
                    val newToken = response.body()

                    App.prefs.temporary_token = newToken!!

                    getPhotos(baseUrl, start)

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

    override fun getPhotos(baseUrl:String, start:Int){

         val homePaging = HomePaging(start, homeView.refreshTimeStamp)
         Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/posts", Parser.toJson(homePaging))

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<HomeResult>(string!!)

                if (start ==0){
                    homeView.clearAdapter()
                }else if(homeView.hasNext){
                    homeView.removePageLoading()
                }

                homeView.hasNext = result.pageable //페이지가 남아있는지 여부

                homeView.refreshTimeStamp = result.created_time //리프리쉬 타임 설정

                homeView.addItems(result.items) //아이템들 추가


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