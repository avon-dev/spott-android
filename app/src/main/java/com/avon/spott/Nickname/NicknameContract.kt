package com.avon.spott.Nickname

import com.avon.spott.BaseView
import com.avon.spott.Data.Token
import com.avon.spott.Data.User

interface NicknameContract {
    interface View:BaseView<Presenter> {
        fun navigateUp()
        fun signUp(result:Boolean)
        fun enableSignUp(enable:Boolean)
        fun getToken(token:Token)
    }

    interface Presenter {
        fun navigateUp()
        fun isNickname(nickname:String)
        fun signUp(baseUrl:String, user: User)
        fun getToken(baseUrl:String, email:String, password:String)
    }
}