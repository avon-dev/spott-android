package com.avon.spott.Email

import android.annotation.SuppressLint
import com.avon.spott.Data.EmailAuth
import com.avon.spott.Data.Number
import com.avon.spott.Utils.*
import retrofit2.HttpException

class EmailPresenter(val emailView: EmailContract.View) : EmailContract.Presenter {

    private val TAG = "EMAILPRESENTER"
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
            emailView.showError("인증번호를 확인해주세요")
        }
    }

    @SuppressLint("CheckResult")
    override fun sendEmail(baseUrl: String, email: String) {
        Retrofit(baseUrl).getNonHeader("/spott/email-authen", Parser.toJson(EmailAuth(1001, email)))
            .subscribe({ response ->
                logd(TAG, response.body())
                val number = response.body()?.let { Parser.fromJson<Number>(it) }

                if(number != null) {
                    if (!number.duplication && !number.code.equals(""))
                        emailView.getNumber(number)
                    else
                        emailView.showError("이미 가입한 이메일입니다")
                }
            }, { throwable ->
                loge(TAG, throwable.message)
                if (throwable is HttpException) {
                    loge(TAG, "http exception : code ${throwable.code()}, message ${throwable.message()}" )
                }
                emailView.showError("다시 시도해주세요")
            })
    }
}
