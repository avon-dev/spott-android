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

//    val adddummy = addDummy() //테스트 코드 추가

    override fun getToken(baseUrl: String, testUrl:String, start:Int) {
        if(App.prefs.temporary_token!=""){
            logd(TAG, "토큰 있음")
            getPhotos(baseUrl, start)
        }else{
            logd(TAG, "토큰 없음")
            Retrofit(testUrl).postNonHeader( "/spott/home/token","")
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

    override fun getPhotos(baseUrl:String, start:Int){ //수정 예정 사항, 여기에 start와 정렬 방식을 보내줘야함.

         val homePaging = HomePaging(start, homeView.refreshTimeStamp)
         Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/home/posts", Parser.toJson(homePaging))

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

        //더미테스트 코드============================================
//        val string  = adddummy
//        val result = Parser.fromJson<HomeResult>(string!!)
//        logd(TAG, "start : " + start)
//        if(homeView.hasNext && start!=0){
//            logd(TAG, "plusssss")
//            homeView.removePageLoading()
//        }else if(start==0 && homeView.refreshTimeStamp!=null){
//            logd(TAG, "refreshing")
//            homeView.refreshTimeStamp=""
//            homeView.clearAdapter()
//        }
//
//        homeView.hasNext = result.hasNext
//
//        homeView.addItems(result.photos)
        //=======================================================
    }

    /*============================= 더미 데이터 넣는 코드=========================================== */
    private fun addDummy() : String{
         val dummyALHome = ArrayList<HomeItem>()
        for(i in 0..4){
            dummyALHome.add(HomeItem("https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg",i))
            dummyALHome.add(HomeItem("https://cdn.pixabay.com/photo/2017/06/23/17/41/morocco-2435391_960_720.jpg",i+1))
        }
        val dummyALHomeResult = HomeResult(true, dummyALHome, "yes")

        val dummyString = "{ payload : "+Parser.toJson(dummyALHomeResult) + "}"

        return dummyString
    }
    /*============================= 더미 데이터 넣는 코드 끝======================================= */


}