package com.avon.spott.Email

import android.util.Log
import com.avon.spott.Data.Number
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.Validator
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
        emailView.isEmail(Validator.validEmail(email))
    }

    override fun sendEmail(email: String) {
        Retrofit().sendEmail(email).subscribe({
            emailView.sendEmail(Parser.fromJson<Number>(it.body()!!))
        }, { throwable ->
            Log.d(TAG, throwable.message)
            if(throwable is HttpException) {
                Log.d(TAG, "http exception code : ${throwable.code()}, http exception response code: ${throwable.response()?.code()}")
            }
        })
    }
}