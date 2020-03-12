package com.avon.spott.Utils

import java.security.cert.Certificate
import javax.crypto.Cipher

// Cipher는 암호화와 암호 해독을 위한 기능을 제공한다.
// Cipehr객체를 생성하기 위해서는 getInstance() 메서드를 호출하고, 요청된 transformation을 전달한다.
// transformation은 주어진 입력에 대해 수행될 작업을 설명하는 문자열이다.
// transformation은 항상 암호 알고리즘을 포함하며, 피드백 모드와 패딩 체계가 뒤따를 수 있다.

// transformation 형태 : "알고리즘/모드/패딩" or "알고리즘"

val TAG = "RSAEncrypt"

fun RSAEncrypt(certificate: Certificate, password:String): ByteArray {

    val textByteArray = password.toByteArray(Charsets.UTF_8)

    val cipher = Cipher.getInstance("RSA/NONE/PKCS1PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, certificate.publicKey)
    val cipherResult = cipher.doFinal(textByteArray)
    logd(TAG, "cipherResult:${cipherResult.size}")

    return cipherResult
}