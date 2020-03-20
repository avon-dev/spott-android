package com.avon.spott.Utils

import android.content.Context
import android.content.SharedPreferences

class PermissionPreferences(context: Context) {

    val PER_FILENAME = "PER"
    val PER_KEY_ID = "perId"


    val per: SharedPreferences =
        context.getSharedPreferences(PER_FILENAME, Context.MODE_PRIVATE)

    var email: String?
        get() = per.getString(PER_KEY_ID, "")
        set(value) =  per.edit().putString(PER_KEY_ID, value).apply()
}