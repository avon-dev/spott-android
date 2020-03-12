package com.avon.spott.EmailLogin

import android.annotation.SuppressLint
import com.avon.spott.Camera.HTTP_BAD_REQUEST
import com.avon.spott.Camera.HTTP_UNAUTHORIZED
import com.avon.spott.Data.Token
import com.avon.spott.Utils.*
import com.google.gson.GsonBuilder
import retrofit2.HttpException
import java.security.cert.Certificate

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

    @SuppressLint("CheckResult")
    override fun signIn(baseurl: String, email: String, password: String, certificate: Certificate) {

        val cipherpw  = RSAEncrypt(certificate, password)
        val encryptpw = cipherpw.contentToString()

        Retrofit(baseurl).signIn("/spott/token", email, encryptpw).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")
            val jsonObj = response.body()
            if(jsonObj != null) {
                val token = GsonBuilder().create().fromJson(jsonObj, Token::class.java)
                emailLoginView.showMainUi(token)
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                val exception = throwable
                loge(TAG, "http exception code: ${exception.code()}, http exception message: ${exception.message()}")
                if(exception.code() == HTTP_BAD_REQUEST) { // 잘못된 요청
                    emailLoginView.showError("이메일, 비밀번호를 확인해주세요")
                } else if (exception.code() == HTTP_UNAUTHORIZED) { // 권한 없을 때
                    emailLoginView.showError("이메일, 비밀번호를 확인해주세요")
                } else {
                    emailLoginView.showError("다시 시도해주세요")
                }
            }
        }, {
//            logd(TAG, "실패")
        })
    }

    @SuppressLint("CheckResult")
    override fun getPublicKey(baseUrl: String, url: String) {
        Retrofit(baseUrl).getNonHeader(url).subscribe({ response ->

            logd(TAG, response.message())
            val raw = response.raw()

            val certificate = raw.handshake()?.peerCertificates().orEmpty()
            if (certificate.size > 0) {
                logd(TAG, "공개키 스트링 : ${certificate.get(0).publicKey.toString()}")
                emailLoginView.getPublicKey(certificate.get(0))
            } else {
                emailLoginView.showMessage(App.ERROR_PUBLICKEY)
            }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
                emailLoginView.showMessage(App.ERROR_PUBLICKEY)
            } else {
                emailLoginView.showMessage(App.ERROR_ERTRY)
            }
        })
    }
}