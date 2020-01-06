package com.avon.spott.Nickname

import com.avon.spott.Data.Result
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

    //"{email:back947@naver.com,nickname:inca,password:123456}"
    // "{\"email\":\"back947@naver.com\",\"nickname\":\"inca\",\"password\":\"123456\"}"
    // Parser.toJson(user)
    override fun signUp(user: User) {
        Retrofit().post("/spott/user", Parser.toJson(user)).subscribe({ response ->
            println("================Resposne==========")
            println("\nresponse code: ${response.code()}\nresponse body : ${response.body()}\n")
            println("===================================")
            val result = response.body()?.let { Parser.fromJson<Result>(it) }
            result?.let { nicknameView.showMainUi(it.result) }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                val exception = throwable
                logd(
                    TAG,
                    "http exception code: ${exception.code()}, http exception response code: ${exception.response()?.code()}"
                )
                exception.printStackTrace()
            }
        })
    }

//    override fun signUp(user: User) {
//        Retrofit().signUp(Parser.toJson(user)).subscribe({ response ->
//            val result = response.body()
//            val payload = Parser.fromJson<Result>(result!!)
//            println("=====================================================")
//            println(response.code())
//            println(response.body())
//            println("=====================================================")
//            logd(TAG, "response.body: ${response.body()}, \npayload: $payload,\nresponse message: ${response.message()}, response code: ${response.code()}")
//            if (payload.result)
//                nicknameView.showMainUi()
//        }, { throwable ->
//            logd(TAG, throwable.message)
//            if (throwable is HttpException) {
//                val exception = throwable
//                logd(
//                    TAG,
//                    "http exception code: ${exception.code()}, http exception response code: ${exception.response()?.code()}"
//                )
//            }
//        })
//    }
}