package com.avon.spott.Email

import com.avon.spott.Data.Number
import com.avon.spott.Data.User
import com.avon.spott.Utils.*
import retrofit2.HttpException

class EmailPresenter(val emailView: EmailContract.View) : EmailContract.Presenter {

    private val TAG = "EMAILPRESENTER"

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

    override fun sendEmail(isEmail:Boolean, baseUrl: String, email: String) {
        if (!isEmail) { // 2020-02-04 수정(민석):  "!" 추가
            emailView.showError("이메일을 확인해주세요")
        } else {
            Retrofit(baseUrl).getNonHeader("/spott/email-authen", Parser.toJson(User(email)))
                .subscribe({ response ->
                    logd(TAG, response.body())
                    val number = response.body()?.let { Parser.fromJson<Number>(it) }
                    if (!number?.code?.equals("")!!) {
                        emailView.getNumber(number)
                    }
                }, { throwable ->
                    loge(TAG, throwable.message)
                    if (throwable is HttpException) {
                        loge(
                            TAG,
                            "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                        )
                    }
                })
        }
    }
}