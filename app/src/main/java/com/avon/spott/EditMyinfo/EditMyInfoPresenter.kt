package com.avon.spott.EditMyinfo

import android.annotation.SuppressLint
import android.net.Uri
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.UserInfo
import com.avon.spott.Utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class EditMyInfoPresenter(val editMyInfoView:EditMyInfoContract.View) : EditMyInfoContract.Presenter {

    private val TAG = "EditMyInfoPresenter"

    init {
        editMyInfoView.presenter = this
    }

    override fun navigateUp() {
        editMyInfoView.navigateUp()
    }

    @SuppressLint("CheckResult")
    override fun getUser(baseUrl: String, token: String) { // 유저정보 가져오기
        Retrofit(baseUrl).get(token, "/spott/users").subscribe({ response ->
            logd(TAG, "response : ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<UserInfo>(it)}
            if(result != null) editMyInfoView.getUserInfo(result)
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }

    override fun isNickname(nickname: String) { // 닉네임을 맞게 입력했는지
        editMyInfoView.validNickname(Validator.validNickname(nickname))
    }

    @SuppressLint("CheckResult")
    override fun changeNickname(baseUrl:String, token:String, nickname: String) {
        val userInfo = UserInfo()
        userInfo.nickname = nickname
        val sending = Parser.toJson(userInfo)
        Retrofit(baseUrl).patch(token, "/spott/users", sending).subscribe({ response ->
            logd(TAG, "response : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if(result != null)
                editMyInfoView.getNickname(result.result)
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun setProfileImage(baseUrl: String, token: String, photoUri: Uri) {
        val file = File(photoUri.path)
        val size = (file.length()/1024).toString() //사이즈 크기 kB
        logd(TAG, "File size : " + size)

        /*----------------서버에 이미지 업로드 테스트용 이미지 2개 생성 ---------------------------------
        * 변경예정사항 : 1. 윤곽선이미지 추가해야함. 2. 이미지 이름 바꿔야함.*/
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        var requestBody: RequestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        val sendImage = MultipartBody.Part.createFormData("profile_image", "p"+timeStamp+".jpg", requestBody)
        /* -------------------------------------------------------------------------------------- */

        Retrofit(baseUrl).patchPhoto(token, "/spott/users", sendImage).subscribe({ response ->
            logd(TAG, "response : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if(result != null)
                editMyInfoView.changedProfile(result.result, photoUri)
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })

    }

    @SuppressLint("CheckResult")
    override fun deleteProfileImage(baseUrl: String, token: String, url:String) {

//        var requestBody: RequestBody = RequestBody.create(null, "null")
        val deleteImage = MultipartBody.Part.createFormData("profile_image", "null")

        Retrofit(baseUrl).patchPhoto(token, "/spott/users", deleteImage).subscribe({ response ->
            logd(TAG, "response : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if(result != null)
                editMyInfoView.deleteProfileImage()
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }

    @SuppressLint("CheckResult")
    override fun withDrawl(baseUrl: String, token: String) {
        Retrofit(baseUrl).delete(token, "/spott/users").subscribe({ response ->
            logd(TAG, "response : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if(result != null)
                editMyInfoView.withDrawl(result.result)

        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }

    override fun signOut(pref:MySharedPreferences) { // 로그아웃하기
        EditMyInfoModel.deleteToken(pref)
        editMyInfoView.loginActivity()
    }
}