package com.avon.spott.NewPassword

import com.avon.spott.BaseView
import java.security.cert.Certificate

interface NewPasswordContract {

    interface View : BaseView<Presenter> {
        fun navigateUp()
        fun isPassword(isPassword:Boolean)
        fun isCheck(isCheck:Boolean)
        fun showWarning()
        fun fixResult(result:Boolean)
        fun getPublicKey(certificate: Certificate)
        fun showMessage(code:Int)
    }

    interface Presenter {
        fun navigateUp()
        fun isPassword(password:String)
        fun isCheck(password:String, checkpw:String)
        fun getPublicKey(baseUrl:String, url:String)
        fun fixPassword(baseUrl:String, email:String, password:String, certificate: Certificate)
    }
}