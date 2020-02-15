package com.avon.spott.ChangePassword

import android.annotation.SuppressLint
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.Password
import com.avon.spott.Utils.*
import retrofit2.HttpException

class ChangePasswordPresenter(val view:ChangePasswordContract.View) : ChangePasswordContract.Presenter {

    private val TAG = "ChangePasswordPresenter"

    init {
        view.presenter = this
    }

    override fun navigateUp() { // 뒤로가기
        view.navigateUp()
    }

    @SuppressLint("CheckResult")
    override fun checkPassword(baseUrl:String, token:String, password: String) {
        val pw = Password(password)
        val sending = Parser.toJson(pw)

        Retrofit(baseUrl).get(token, "/spott/users/password", sending).subscribe({ response ->
            logd(TAG, "response body: ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }

            if(result != null){
                view.checkPassword(result.result)
            }
        }, { throwable ->
            loge(TAG, "throwable: ${throwable.message}")
            if(throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, msg: ${throwable.message()}")
            }
        })
    }

    override fun isPassword(password: String) {
        view.isPassword(Validator.validPassword(password))
    }

    override fun isCheck(password: String, checkpw: String) {
        view.isCheck(password.equals(checkpw))
    }

    @SuppressLint("CheckResult")
    override fun changePassword(baseUrl: String, token: String, password: String) {
        val pw = Password(password)
        val sending = Parser.toJson(pw)

        Retrofit(baseUrl).patch(token, "/spott/users/password", sending).subscribe({ response ->
            logd(TAG, "response body: ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it)}
            if(result != null)
                view.changedPassword(result.result)
        }, { throwable ->
            loge(TAG, "throwable: ${throwable.message}")
            if(throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, msg: ${throwable.message()}")
            }
        })
    }
}