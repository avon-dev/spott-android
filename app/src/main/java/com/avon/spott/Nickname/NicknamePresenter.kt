package com.avon.spott.Nickname

import android.annotation.SuppressLint
import com.avon.spott.Data.NicknmaeResult
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Data.User
import com.avon.spott.Utils.*
import com.google.gson.GsonBuilder
import retrofit2.HttpException
import java.security.cert.Certificate

class NicknamePresenter(val nicknameView: NicknameContract.View) : NicknameContract.Presenter {

    private val TAG: String = "NicknamePresenter"

    init {
        nicknameView.presenter = this
    }

    override fun navigateUp() {
        nicknameView.navigateUp()
    }

    override fun isNickname(nickname: String) {
        nicknameView.enableSignUp(Validator.validNickname(nickname))
    }

    override fun getToken(baseUrl: String, email: String, password: String) {}

    // 소셜 로그인
    @SuppressLint("CheckResult")
    override fun signUp(baseUrl: String, url: String, socialUser: SocialUser) {
        val sending = Parser.toJson(socialUser)
        logd(TAG, sending)
        Retrofit(baseUrl).postFieldNonHeader(url, sending).subscribe({ response ->
            logd(TAG, "social signUp() code: ${response.code()}, response body : ${response.body()}")

            val result = response.body()?.let { Parser.fromJson<NicknmaeResult>(it) }
            if (result != null) {
                if(result.sign_up) {
                    nicknameView.socialSignup()
                } else {
                    nicknameView.showMessage(NicknameActivity.ERROR_DUPLICATION)
                }
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            nicknameView.showMessage(App.ERROR_ERTRY)
        })
    }

    @SuppressLint("CheckResult")
    override fun getToken(baseUrl: String, url: String, socialUser: SocialUser) {
        logd(TAG, "${socialUser.email}, ${socialUser.user_type}")

        Retrofit(baseUrl).signIn("/spott/token", socialUser.email, socialUser.user_type!!).subscribe({ response ->
            logd(TAG, "getToken() : response code: ${response.code()}, response body : ${response.body()}")

            val jsonObj = response.body()
            if (jsonObj != null) {
                val token = GsonBuilder().create().fromJson(jsonObj, Token::class.java)
                nicknameView.getToken(token)
            }
        }, { throwable ->
            loge(TAG, "getToken(): ${throwable.message}")
            if (throwable is HttpException) {
                loge(
                    TAG,
                    "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}"
                )
            }
        })
    }

    // 이메일 로그인
    @SuppressLint("CheckResult")
    override fun getPublicKey(baseUrl: String, url: String) {
        Retrofit(baseUrl).getNonHeader(url).subscribe({ response ->

            logd(TAG, response.message())
            val raw = response.raw()

            val certificate = raw.handshake()?.peerCertificates()?.get(0)
            if (certificate != null) {
                nicknameView.getPublicKey(certificate)
            } else {
                nicknameView.showMessage(App.ERROR_PUBLICKEY)
            }
            nicknameView.addClickListener()
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}")
                nicknameView.showMessage(throwable.code())
            } else {
                nicknameView.showMessage(App.ERROR_PUBLICKEY)
            }
            nicknameView.addClickListener()
        })
    }

    @SuppressLint("CheckResult")
    override fun signUp(baseUrl: String, user: User, certificate: Certificate) {

        logd(TAG, "user.password : "+user.password)

        val cipherpw  = RSAEncrypt(certificate, user.password!!)
        val cipherUser = User(user.email)
        cipherUser.password = user.password
        cipherUser.nickname = user.nickname

        cipherUser.password = cipherpw.contentToString()

        logd(TAG, "PASER : " + Parser.toJson(cipherUser)) //테스트테스트
        Retrofit(baseUrl).postFieldNonHeader("/spott/account", Parser.toJson(cipherUser))
            .subscribe({ response ->
                logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")
                val result = response.body()?.let { Parser.fromJson<NicknmaeResult>(it) }

                val certificate = response.raw().handshake()?.peerCertificates()?.get(0)

                if(certificate == null) {
                    nicknameView.showMessage(App.ERROR_PUBLICKEY)
                } else if (result != null && result.sign_up)  {
                    nicknameView.signUp(certificate)
                } else {
                    nicknameView.showMessage(App.ERROR_ERTRY)
                }
            }, { throwable ->
                loge(TAG, throwable.message)
                if (throwable is HttpException) {
                    val exception = throwable
                    loge(
                        TAG,
                        "http exception code: ${exception.code()}, http exception message: ${exception.message()}"
                    )
                }
            })
    }

    @SuppressLint("CheckResult")
    override fun getToken(baseUrl: String, email: String, password: String, usertype:Int, certificate: Certificate) {

        val cipherpw  = RSAEncrypt(certificate, password)
        val encryptpw = cipherpw.contentToString()

        Retrofit(baseUrl).signIn("/spott/token", email, encryptpw, usertype).subscribe({ response ->
            logd(TAG, "getToken() : response code: ${response.code()}, response body : ${response.body()}")

            val jsonObj = response.body()
            if (jsonObj != null) {
                val token = GsonBuilder().create().fromJson(jsonObj, Token::class.java)
                nicknameView.getToken(token)
            }
        }, { throwable ->
            loge(TAG, "getToken(): + ${throwable.message}")
            if (throwable is HttpException) {
                loge(
                    TAG,
                    "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}"
                )
            }
        })
    }
}
