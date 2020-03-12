package com.avon.spott.EmailLogin

import com.avon.spott.BaseView
import com.avon.spott.Data.Token
import java.security.cert.Certificate

interface EmailLoginContract {
    interface View : BaseView<Presenter> {
        fun showMainUi(token: Token)
        fun showFindPWUi()
        fun navigateUp()
        fun showError(error:String)
        fun getPublicKey(certificate: Certificate)
        fun showMessage(msgCode:Int)
    }

    interface Presenter {
        fun openFindPW()
        fun navigateUp()
        fun getPublicKey(baseUrl:String, url:String)
        fun signIn(baseurl:String, email:String, password:String, certificate: Certificate)
    }
}