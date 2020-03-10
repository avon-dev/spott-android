package com.avon.spott.Splash

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.avon.spott.Data.IntResult
import com.avon.spott.Data.Token
import com.avon.spott.Login.LoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.*
import com.google.gson.GsonBuilder
import retrofit2.HttpException
import java.security.PublicKey
import java.security.cert.Certificate
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.or


class SplashActivity : Activity() {

    val TAG = "forSplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({

            // 자동 로그인
            // 1. shared확인
            // 2-1. main으로 이동
            // 2-2. 로그인 액티비티 실행
            val shared = MySharedPreferences(this)
            val access = shared.token
            val refresh = shared.refresh
            if(!access.equals("") and !refresh.equals("")) { // 토큰이 있으면
                availableToken(getString(R.string.baseurl), "/spott/token/verify", Token(refresh, access))
            }else{
                showMainUi(false)
            }


//            Intent(this@SplashActivity, LoginActivity::class.java).let { startActivity(it) }
//            finish()
        }, 1000) //로딩 주기

        // 자동 로그인 로직 넣기

    }

    @SuppressLint("CheckResult")
    private fun availableToken(baseUrl:String, url:String, token: Token) {
        val sending = Parser.toJson(token)

        var auto = false

        Retrofit(baseUrl).postFieldNonHeader(url, sending).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")

            val header = response.headers()


            val raw = response.raw()

            val certificate = raw.handshake()?.peerCertificates().orEmpty()
            if(certificate!= null)
                encrypt("seunghyun1!", certificate.get(0))

//            val publicKey = raw.handshake()?.peerCertificates()?.get(0)?.publicKey
//            if(publicKey != null)
//                encrypt("seunghyun1!", publicKey)


            val result = response.body()?.let { Parser.fromJson<IntResult>(it) }
            if(result != null) {
                logd(TAG, "result code: ${result.result}")
                // 액세스 토큰 활성 1002
                if (result.result == 1002) {
                    logd(TAG, "토큰 사용 가능")
                    auto = true
                } else if (result.result == 1003) {
                    // 액세스 토큰 만료 1003
                    App.prefs.token = result.access.toString()
                    logd(TAG, "액세스 만료")
                    auto = true

                } else if (result.result == 1001) {
                    // 리프레시 토큰 만료 1001 -> 그냥 로그인 로직을 타야 함
                    logd(TAG, "리프레시 만료")
                }
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code: ${throwable.code()}, message: ${throwable.message()}")
            }
        }, {
            showMainUi(auto)
        })
    }

    private fun showMainUi(auto:Boolean) {
        logd(TAG, "showMainUi")
        var intent:Intent
        if(auto) {
            intent = Intent(this@SplashActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            intent = Intent(this@SplashActivity, LoginActivity::class.java)
        }

        startActivity(intent)
    }

    /* test code [Start]*/
    fun encrypt(pw:String, certificate: Certificate) {
        val charset = Charsets.UTF_8

        val plaintext:ByteArray = pw.toByteArray(charset)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
//        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext:ByteArray = cipher.doFinal(plaintext)
        val iv:ByteArray = cipher.iv

        logd(TAG, "pw: $pw")
        logd(TAG, "plaintext: ${plaintext.toString()}")
        logd(TAG, "ciphertext: ${ciphertext.toString()}")
        logd(TAG, "iv: $iv")

        val bytearraytobinarystring = byteArrayToBinaryString(ciphertext)
        val binarystringtonytearray = binaryStringToByteArray(bytearraytobinarystring)

        val cipher3 = Cipher.getInstance("RSA")
        cipher3.init(Cipher.ENCRYPT_MODE, certificate.publicKey)
        val cipher3text = cipher3.doFinal(key.encoded)
//        val strcipher3text = byteArrayToBinaryString(cipher3text)
//        val bytearray3text = binaryStringToByteArray(strcipher3text)
        val ivtext = cipher3.doFinal(iv)

        val secretKey = SecretKeySpec(cipher3text, "AES")
        val ivSpec = IvParameterSpec(ivtext)
//        val secretKey = SecretKeySpec(bytearray3text, "AES")
        var keyjson2 = GsonBuilder().create().toJson(secretKey)
        var fromJson = GsonBuilder().create().fromJson(keyjson2, SecretKeySpec::class.java)

        // GsonBuilder()는 Gson()보다 제한적인 느낌이다.

        val a = 10

//        if(strcipher2text.equals(strcipher3text))
            logd(TAG, "둘이 같음!!")
//        else
            logd(TAG, "둘이 다름!!")


    }

    fun encrypt(pw:String, publicKey: PublicKey) {
        val charset = Charsets.UTF_8

        val plaintext:ByteArray = pw.toByteArray(charset)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val ciphertext:ByteArray = cipher.doFinal(plaintext)
        val iv:ByteArray = cipher.iv

        var dicipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        dicipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        var deciphertext:ByteArray = dicipher.doFinal(ciphertext)


        logd(TAG, "pw: $pw")
        logd(TAG, "plaintext: ${plaintext.toString()}")
        logd(TAG, "ciphertext: ${ciphertext.toString()}")
//        logd(TAG, "deciphertext: ${deciphertext.toString(charset)}")
        logd(TAG, "iv: $iv")

        val bytearraytobinarystring = byteArrayToBinaryString(ciphertext)
        val binarystringtonytearray = binaryStringToByteArray(bytearraytobinarystring)

        logd(TAG, "bytearray->string:${bytearraytobinarystring}")
        logd(TAG, "String->bytearray:${binarystringtonytearray.toString()}")

        logd(TAG, "ciphertext.contentToString:${ciphertext.contentToString()}")


        // 복호화
        logd(TAG, "복호화")
        logd(TAG, "deciphertext: ${deciphertext.toString(charset)}")
        deciphertext = dicipher.doFinal(binarystringtonytearray)
        logd(TAG, "deciphertext: ${deciphertext.toString(charset)}")
//
        val str = "cipher" + ciphertext.contentToString()
    }

    // 바이너리 배열 -> 스트링
    fun byteArrayToBinaryString(b: ByteArray): String {
        var sb = StringBuilder()
        for(i in 0..b.size-1) {
            logd(TAG, "$i")
            sb.append(byteToBinaryString(b[i]))
        }

        return sb.toString()
    }

    // 바이너리 바이트 -> 스트링
    fun byteToBinaryString(n: Byte): String {
        val sb = StringBuilder("00000000")
        for (bit in 0..7) {
            if (n.toInt() shr bit and 1 > 0) {
                sb.setCharAt(7 - bit, '1')
            }
        }
        return sb.toString()
    }

    /**
     * 바이너리 스트링을 바이트배열로 변환
     *
     * @param s
     * @return
     */
    fun binaryStringToByteArray(s: String): ByteArray {
        val count = s.length / 8
        val b = ByteArray(count)
        for (i in 1..count) {
            val t = s.substring((i - 1) * 8, i * 8)
            b[i - 1] = binaryStringToByte(t)
        }
        return b
    }

    /**
     * 바이너리 스트링을 바이트로 변환
     *
     * @param s
     * @return
     */
    fun binaryStringToByte(s: String): Byte {
        var ret: Byte = 0
        var total: Byte = 0
        for (i in 0..7) {
            ret = if (s[7 - i] == '1') (1 shl i).toByte() else 0
            total = (ret or total).toByte()
        }
        return total
    }


    /* test code [End]*/


}
