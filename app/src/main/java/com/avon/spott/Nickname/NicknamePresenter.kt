package com.avon.spott.Nickname

import com.avon.spott.Data.NicknmaeResult
import com.avon.spott.Data.User
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class NicknamePresenter(val nicknameView: NicknameContract.View) : NicknameContract.Presenter {

    private val TAG: String = "NicknamePresenter"

    init {
        nicknameView.presenter = this
    }

    override fun navigateUp() {
        nicknameView.navigateUp()
    }

    override fun isNickname(nickname: String) {
        nicknameView.enableSignUp(nickname.length > 3)
    }

    override fun signUp(baseUrl: String, user: User) {
        Retrofit(baseUrl).post("/spott/user", Parser.toJson(user)).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<NicknmaeResult>(it) }
            if (result != null) {
                nicknameView.showMainUi(result.result)
            }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                val exception = throwable
                logd(TAG, "http exception code: ${exception.code()}, http exception message: ${exception.message()}")
            }
        })
    }
}