package com.avon.spott.EditMyinfo

import com.avon.spott.Utils.MySharedPreferences
import com.facebook.login.LoginManager

class EditMyInfoModel {
    companion object {
        fun deleteToken(pref:MySharedPreferences) {
            pref.deleteToken()
        }
        fun signOutwithFacebook() {
            LoginManager.getInstance().logOut()
        }
    }
}