package com.avon.spott.Email

import android.annotation.SuppressLint
import com.avon.spott.Data.EmailAuth
import com.avon.spott.Data.Number
import com.avon.spott.Utils.*
import retrofit2.HttpException

class EmailPresenter(val emailView: EmailContract.View) : EmailContract.Presenter {

    private val TAG = "EmailPresenter"
    private val ACTION_EMAIL = 1001

    init {
        emailView.presenter = this
    }

    override fun openPassword() {
        emailView.showPasswordUi()
    }

    override fun navigateUp() {
        emailView.navigateUp()
    }

    override fun isEmail(email: String) {
        emailView.validEmail(Validator.validEmail(email))
    }

    override fun isNumber(number: String) {
        emailView.validNumber(Validator.validNumber(number))
    }

    override fun confirm(number: Number, str: String) {
        if (number.code.equals(str)) {
            emailView.showPasswordUi()
        } else {
            emailView.showMessage(EmailActivity.CHECK_NUMBER)
        }
    }

    @SuppressLint("CheckResult")
    override fun sendEmail(baseUrl: String, email: String) {
        emailView.showLoading()

        Retrofit(baseUrl).getNonHeader("/spott/email-auth", Parser.toJson(EmailAuth(ACTION_EMAIL, email)))
            .subscribe({ response ->
                emailView.hideLoading()
                logd(TAG, response.body())
                val number = response.body()?.let { Parser.fromJson<Number>(it) }

                if(number != null) {
                    if(number.duplication) { // 중복된 이메일
                        emailView.showMessage(EmailActivity.ERROR_DUPLICATION_EMAIL)
                    } else { // 사용 가능한 이메일
                        emailView.getNumber(number)
                    }
                }
            }, { throwable ->
                emailView.hideLoading()

                val code:Int
                if (throwable is HttpException) {
                    loge(TAG, "http exception : code ${throwable.code()}, message ${throwable.message()}" )
                    code = throwable.code()
                } else {
                    loge(TAG, throwable.message)
                    code = App.ERROR_ERTRY
                }
                emailView.showMessage(code)
            })
    }
}
