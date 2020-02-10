package com.avon.spott.Utils

import android.util.Log

fun logd(TAG:String, msg:String?) {
    Log.d(TAG, msg)
}

fun loge(TAG:String, msg:String?) {
    Log.e(TAG, msg)
}

fun loge(TAG:String, msg:String, cause:Throwable?) {
    Log.e(TAG, msg, cause)
}