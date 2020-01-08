package com.avon.spott.Nickname

import com.avon.spott.BaseView
import com.avon.spott.Data.User

interface NicknameContract {
    interface View:BaseView<Presenter> {
        fun navigateUp()
        fun showMainUi(result:Boolean)
        fun enableSignUp(enable:Boolean)
    }

    interface Presenter {
        fun navigateUp()
        fun isNickname(nickname:String)
        fun signUp(baseUrl:String, user: User)
    }
}