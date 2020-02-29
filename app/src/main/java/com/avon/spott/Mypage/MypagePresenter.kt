package com.avon.spott.Mypage

import com.avon.spott.Data.MypageResult
import com.avon.spott.Data.Public
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.google.android.gms.maps.model.LatLng
import retrofit2.HttpException

class MypagePresenter(val mypageView:MypageContract.View):MypageContract.Presenter {

    private val TAG = "MypagePresenter"

    init{mypageView.presenter = this}

    override fun openAddPhoto(mCropPath: String) {
        mypageView.showAddPhotoUi(mCropPath)
    }

    override fun openAlarm() {
        mypageView.showAlarmUi()
    }

    override fun openEditMyInfo() {
        mypageView.showEditMyInfoUi()
    }

    override fun openPhoto(id:Int) {
        mypageView.showPhotoUi(id)
    }

    override fun clickAddPhoto(){
        if (!mypageView.checkPermission()) {
            mypageView.showPermissionDialog()
            return
        }
        mypageView.openGallery()
    }


    override fun getMyphotos(baseUrl: String) {

        Retrofit(baseUrl).get(App.prefs.token, "/spott/users/my-posts",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<MypageResult>(string!!)

                mypageView.clearAdapter()

                mypageView.setUserInfo(result.user.nickname, result.user.profile_image, result.user.is_public)

                mypageView.setNotiCount(result.is_confirmation)

                if(result.posts.size==0){
                    mypageView.noPhoto()
                    mypageView.movePosition(LatLng(37.56668, 126.97843), 14f) //등록한 사진이 없으면 구글맵 카메라 서울시청으로 이동
                }else{
                    mypageView.movePosition(LatLng(result.posts[0].latitude, result.posts[0].longitude), 11f)
                    //등록한 사진이 있으면 가장 최신 등록된 사진의 위치로 이동
                }

                mypageView.addItems(result.posts)

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

    override fun getNotiCount(baseUrl: String) {
        Retrofit(baseUrl).get(App.prefs.token, "/spott/users/my-posts",  "")
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<MypageResult>(string!!)

                mypageView.setNotiCount(result.is_confirmation)

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

    override fun changePublic(baseUrl: String, isPublic: Boolean) {

        val public = Public(!isPublic)
        logd(TAG, "public sending : "+Parser.toJson(public))

        Retrofit(baseUrl).patch(App.prefs.token, "/spott/mypage",  Parser.toJson(public))
            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<Public>(string!!)

                mypageView.showPublic(result.is_public)

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
                mypageView.showErrorToast()
            })
    }



}