package com.avon.spott.NewPassword

import android.annotation.SuppressLint
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.User
import com.avon.spott.Utils.*
import retrofit2.HttpException

class NewPasswordPresenter(val newPasswordView: NewPasswordContract.View) :
    NewPasswordContract.Presenter {

    private val TAG = "NewPasswordPresenter"

    init {
        newPasswordView.presenter = this
    }

    override fun navigateUp() {
        newPasswordView.navigateUp()
    }

    override fun isPassword(password: String) {
        val isPassword = Validator.validPassword(password)
        newPasswordView.isPassword(isPassword)
        newPasswordView.showWarning()
    }

    override fun isCheck(password: String, checkpw: String) {
        val isCheck = checkpw.equals(password)
        newPasswordView.isCheck(isCheck)
        newPasswordView.showWarning()
    }

    @SuppressLint("CheckResult")
    override fun fixPassword(baseUrl: String, email: String, password: String) {
        val user = User(email, password)

        val sending = Parser.toJson(user)

        Retrofit(baseUrl).patchNonHeader("/spott/account", sending).subscribe({ response ->
            logd(TAG, "response: ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if (result != null) {
                newPasswordView.fixResult(result.result)
            }
        }, { throwable ->
            loge(TAG, "${throwable.message}")
            if (throwable is HttpException) {
                loge(TAG, "code: ${throwable.code()}, msg: ${throwable.message()}")
                newPasswordView.showMessage(throwable.code())
            }
        })
    }
}