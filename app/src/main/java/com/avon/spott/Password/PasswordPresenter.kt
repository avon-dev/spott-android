package com.avon.spott.Password

import com.avon.spott.Utils.Validator
import com.avon.spott.Utils.logd
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import kotlin.experimental.or

class PasswordPresenter(val numberView: PasswordContract.View) : PasswordContract.Presenter {

    private val TAG = "PasswordPresenter"

    init {
        numberView.presenter = this
    }

    override fun navigateUp() {
        numberView.navigateUp()
    }

    override fun openNickname() {
        numberView.showNicknameUi()
    }

    override fun isPassword(password: String) {
        numberView.isPassword(Validator.validPassword(password))
    }

    override fun isPassword(password: String, check: String) {
        numberView.isCheck(check.equals(password))
        numberView.showWarning()
    }

    fun encrypt(pw:String) {
        val charset = Charsets.UTF_8

        val plaintext:ByteArray = pw.toByteArray(charset)
        val keygen = KeyGenerator.getInstance("AES")
        keygen.init(256)
        val key: SecretKey = keygen.generateKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val ciphertext:ByteArray = cipher.doFinal(plaintext)
        val iv:ByteArray = cipher.iv

        var dicipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        dicipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
//        cipher.init(Cipher.DECRYPT_MODE, key)
        var deciphertext:ByteArray = dicipher.doFinal(ciphertext)



        logd(TAG, "pw: $pw")
        logd(TAG, "plaintext: ${plaintext.toString()}")
        logd(TAG, "ciphertext: ${ciphertext.toString()}")
        logd(TAG, "deciphertext: ${deciphertext.toString(charset)}")
        logd(TAG, "iv: $iv")

        val md = MessageDigest.getInstance("SHA-256")
        val digest:ByteArray = md.digest(plaintext)

        logd(TAG, "digest:$digest")


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


}
