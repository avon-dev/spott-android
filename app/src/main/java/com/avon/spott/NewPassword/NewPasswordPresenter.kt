package com.avon.spott.NewPassword

import android.annotation.SuppressLint
import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.User
import com.avon.spott.Utils.*
import retrofit2.HttpException
import java.security.cert.Certificate

class NewPasswordPresenter(val newPasswordView: NewPasswordContract.View) :
    NewPasswordContract.Presenter {

    private val TAG = "NewPasswordPresenter"

    init {
        newPasswordView.presenter = this
    }

    override fun navigateUp() {
        newPasswordView.navigateUp()
    }

    override fun isPassword(password: String) {
        val isPassword = Validator.validPassword(password)
        newPasswordView.isPassword(isPassword)
    }

    override fun isCheck(password: String, checkpw: String) {
        val isCheck = checkpw.equals(password)
        newPasswordView.isCheck(isCheck)
        newPasswordView.showWarning()
    }

    @SuppressLint("CheckResult")
    override fun fixPassword(baseUrl: String, email: String, password: String, certificate: Certificate) {
        val user = User(email, password)

        val cipherpw  = RSAEncrypt(certificate, user.password!!)
        val cipherUser = User(user.email)
        cipherUser.password = user.password
        cipherUser.nickname = user.nickname

        cipherUser.password= cipherpw.contentToString()

        val sending = Parser.toJson(user)

        Retrofit(baseUrl).patchNonHeader("/spott/account", sending).subscribe({ response ->
            logd(TAG, "response: ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<BooleanResult>(it) }
            if (result != null) {
                newPasswordView.fixResult(result.result)
            }
        }, { throwable ->
            loge(TAG, "${throwable.message}")
            if (throwable is HttpException) {
                loge(TAG, "code: ${throwable.code()}, msg: ${throwable.message()}")
                newPasswordView.showMessage(throwable.code())
            } else {
                newPasswordView.showMessage(App.ERROR_ERTRY)
            }
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
                newPasswordView.getPublicKey(certificate.get(0))
            } else {
                newPasswordView.showMessage(App.ERROR_PUBLICKEY)
            }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
                newPasswordView.showMessage(throwable.code())
            } else {
                newPasswordView.showMessage(App.ERROR_PUBLICKEY)
            }
        })
    }
}