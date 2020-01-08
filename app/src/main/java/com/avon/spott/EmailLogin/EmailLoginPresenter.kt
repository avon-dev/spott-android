package com.avon.spott.EmailLogin

import com.avon.spott.Data.Token
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.Validator
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class EmailLoginPresenter(val emailLoginView: EmailLoginContract.View) :
    EmailLoginContract.Presenter {

    private val TAG = "EmailLoginPresenter"

    init {
        emailLoginView.presenter = this
    }

    override fun openFindPW() {
        emailLoginView.showFindPWUi()
    }

    override fun navigateUp() {
        emailLoginView.navigateUp()
    }

    override fun isEmail(email: String) {
        emailLoginView.isEmail(Validator.validEmail(email))
    }

    override fun isPassword(pw: String) {
        emailLoginView.isPassword(Validator.validPassword(pw))
    }

    override fun signIn(baseurl:String, email: String, password: String) {
        Retrofit(baseurl).signIn("/spott/token", email, password).subscribe({ response ->
            logd(TAG, response.body())
            response.body()?.let {
                val token = Parser.fromJson<Token>(it)
                emailLoginView.showMainUi(token)
            }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                logd(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        })
    }
}