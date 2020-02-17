package com.avon.spott.EditMyinfo

import com.avon.spott.Utils.MySharedPreferences

class EditMyInfoModel {
    companion object {
        fun deleteToken(pref:MySharedPreferences) {
            pref.deleteToken()
        }
    }
}