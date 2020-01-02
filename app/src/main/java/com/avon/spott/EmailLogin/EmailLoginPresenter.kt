package com.avon.spott.EmailLogin

import android.util.Log
import com.avon.spott.Data.Token
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.ValidatorModel
import retrofit2.HttpException

class EmailLoginPresenter(val emailLoginView: EmailLoginContract.View) :
    EmailLoginContract.Presenter {

    private val TAG = "EmailLoginPresenter"

    init {
        emailLoginView.presenter = this
    }

    override fun openMain() {
        emailLoginView.showMainUi()
    }

    override fun openFindPW() {
        emailLoginView.showFindPWUi()
    }

    override fun navigateUp() {
        emailLoginView.navigateUp()
    }

    override fun isEmail(email: String) {
        emailLoginView.isEmail(ValidatorModel.validEmail(email))
    }

    override fun isPassword(pw: String) {
        emailLoginView.isPassword(ValidatorModel.validPassword(pw))
    }

    override fun signIn(email: String, password: String) {
        Retrofit().signIn(email, password).subscribe({response ->
            Log.d(TAG, response.toString())
            // shared저장생략
            if (response.isSuccessful) {
                emailLoginView.showMainUi()
                Log.d(TAG, "response.isSuccessful(true) : ${response.code()}")
            } else {
                Log.d(TAG, "response.isSuccessful(false) : ${response.code()}")
            }
        }, { throwable ->
            Log.d(TAG, throwable.message)
            if (throwable is HttpException) {
                Log.d(TAG, throwable.code().toString())
            }
        })
    }

    override fun authentication() {
        Retrofit().token?.subscribe({ response ->
            // Response<T> 형태이므로 꺼내줘야한다.
            if (response.isSuccessful) {
                // response.body()로 원래 의도한 타입을 꺼낸다.
                val result = Parser.fromJson<Token>(response.body()!!)
                emailLoginView.showMainUi()
            } else {

            }
            /*
                  Response.isSuccessful()한다는 건 성공이 아닌 값도 subscribe()메소드의 onSuccess callback으로 전달된다는 의미이다.
                  기본형태로 구성한 방식의 경우 성공만 onSuccess callback으로 전달되고 그외의 실패의 상황에서는 onError callback으로 전달받는 형태였는데 onSuccess callback에서도 error를 확인해야한다.
                  이 곳에서 성공/실패 처리를 해줘도 되지만 실패의 경우 onError callback으로 전달하는 것이 의미상 더 부합하기 때문에 Retrofit Method에서 예외처리를 좀 더 해주도록 하겠습니다.
                  RxJava에는 map()이라는 메소드가 있습니다. Java8과 kotlin Stream API와 동일한 명칭인 만큼 기능도 동일합니다.
                  서버에서 전달받은 결과객체를 map()을 통해 전처리를 해줍니다.

                  map()을 통해 전달받은 Response<T> 객체에서 isSuccessful을 확인하여 true일 경우는 그대로 전달을 하고 false일 경우는 HttpException을 발생시키도록 하였습니다.
                  HttpException의 경우 생성자로 Response객체를 전달받게 됩니다.
                  이렇게 되는 경우 Http response code가 2xx가 아닌 경우는 모두 HttpException이 발생하게 됩니다.
                */
        }, { throwable ->
            emailLoginView.showError("서버요청중에 오류가 발생했습니다.\n ${throwable.message}")
            if (throwable is HttpException) {
                val exception = throwable
                exception.code() // HttpException의 경우
            }
        })
    }
}