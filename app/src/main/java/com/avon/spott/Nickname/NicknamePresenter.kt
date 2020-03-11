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
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

    @SuppressLint("CheckResult")
    override fun signUp(baseUrl: String, user: User) {
        logd(TAG, "PASER : "+Parser.toJson(user)) //테스트테스트
            Retrofit(baseUrl).postFieldNonHeader("/spott/account", Parser.toJson(user)).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<NicknmaeResult>(it) }
            if (result != null) {
                nicknameView.signUp(result.sign_up)
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
    override fun signUp(baseUrl: String, url: String, socialUser: SocialUser) {
        Retrofit(baseUrl).postFieldNonHeader(url, Parser.toJson(socialUser)).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")

        }, { throwable ->
            loge(TAG, throwable.message)

        })
    }

    @SuppressLint("CheckResult")
    override fun getToken(baseUrl: String, email: String, password: String) {
        Retrofit(baseUrl).signIn("/spott/token", email, password).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")

            val jsonObj = response.body()
            if(jsonObj != null) {
                val token = GsonBuilder().create().fromJson(jsonObj, Token::class.java)
                nicknameView.getToken(token)
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(
                    TAG,
                    "http exception code: ${throwable.code()}, http exception message: ${throwable.message()}"
                )
            }
        })
    }

    fun encrypt(pw:String, certificate: Certificate): ArrayList<String> {
        val charset = Charsets.UTF_8

        val plaintext:ByteArray = pw.toByteArray(charset)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext:ByteArray = cipher.doFinal(plaintext)
        val iv:ByteArray = cipher.iv

        val cipher3 = Cipher.getInstance("RSA")
        cipher3.init(Cipher.ENCRYPT_MODE, certificate.publicKey)
        val cipher3text = cipher3.doFinal(key.encoded)
        val ivtext = cipher3.doFinal(iv)

        val secretKey = SecretKeySpec(cipher3text, "AES")
        val ivSpec = IvParameterSpec(ivtext)
        var keyjson2 = GsonBuilder().create().toJson(secretKey)
        var ivjson = GsonBuilder().create().toJson(ivSpec)

        // GsonBuilder()는 Gson()보다 제한적인 느낌이다.
        var array = ArrayList<String>()

        array.add(keyjson2)
        array.add(ivjson)
        array.add(GsonBuilder().create().toJson(ciphertext))

        return array
    }

    @SuppressLint("CheckResult")
    override fun getPublicKey(baseUrl: String, url: String) {
        Retrofit(baseUrl).getNonHeader(url).subscribe({ response ->

            logd(TAG, response.message())
            val raw = response.raw()

            val certificate = raw.handshake()?.peerCertificates().orEmpty()
            if(certificate!= null) {
                nicknameView.getPublicKey(certificate.get(0))
                logd(TAG, "certificate 얻음")
                logd(TAG, "공개키 스트링 : ${certificate.get(0).publicKey.toString()}")
            }
        }, { throwable ->

            logd(TAG, throwable.message)

        })
    }

    override fun test(baseUrl: String, url: String, certificate: Certificate, user: User) {

        val array = encrypt(user.password!!, certificate)

        val key = array.get(0)
        val iv = array.get(1)
        val password = array.get(2)

        Retrofit(baseUrl).postNonHeader(url, key, iv, password).subscribe({

        }, {

        })
    }
}