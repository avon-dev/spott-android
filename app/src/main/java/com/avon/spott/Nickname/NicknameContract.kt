package com.avon.spott.Nickname

import com.avon.spott.BaseView
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Data.User
import java.security.cert.Certificate

interface NicknameContract {
    interface View:BaseView<Presenter> {
        fun navigateUp()
        fun signUp(certificate: Certificate)
        fun enableSignUp(enable:Boolean)
        fun getToken(token:Token)
        fun getPublicKey(certificate: Certificate)
        fun showLoading()
        fun hideLoading()
        fun socialSignup()
        fun showMessage(msgCode:Int)
        fun addClickListener()
    }

    interface Presenter {
        fun navigateUp()
        fun isNickname(nickname:String)
        fun signUp(baseUrl:String, user: User, certificate: Certificate)
        fun signUp(baseUrl:String, url:String, socialUser: SocialUser)
        fun getToken(baseUrl:String, email:String, password:String)
        fun getToken(baseUrl:String, email:String, password:String, usertype:Int, certificate: Certificate)
        fun getPublicKey(baseUrl:String, url:String)
        fun getToken(baseUrl:String, url:String, socialUser: SocialUser)
    }
}