package com.avon.spott.FindPW

import android.annotation.SuppressLint
import com.avon.spott.Data.EmailAuth
import com.avon.spott.Data.Number
import com.avon.spott.Utils.*
import retrofit2.HttpException

class FindPWPresenter(val findPWView: FindPWContract.View) : FindPWContract.Presenter {

    private val TAG = "FindPWPresenter"
    private val ACTION_EMAIL = 1002

    init {
        findPWView.presenter = this
    }

    override fun navigateUp() {
        findPWView.navigateUp()
    }

    override fun isEmail(email: String) {
        findPWView.isEmail(Validator.validEmail(email))
    }

    @SuppressLint("CheckResult")
    override fun sendEmail(email: String, baseUrl: String) {
        findPWView.showLoading()

        Retrofit(baseUrl).getNonHeader("/spott/email-auth", Parser.toJson(EmailAuth(ACTION_EMAIL, email)))
            .subscribe({ response ->
                findPWView.hideLoading()

                logd(TAG, response.body())
                val number = response.body()?.let { Parser.fromJson<Number>(it) }
                if (number != null) {
                    if (number.registration)
                        findPWView.getNumber(number)
                    else
                        findPWView.showMessage(FindPWActivity.ERROR_NON_EMAIL)
                }
            }, { throwable ->
                findPWView.hideLoading()

                loge(TAG, throwable.message)
                if (throwable is HttpException) {
                    loge(
                        TAG,
                        "http exception : code ${throwable.code()}, message ${throwable.message()}"
                    )
                }
                findPWView.showMessage(App.ERROR_ERTRY)
            })
    }
}