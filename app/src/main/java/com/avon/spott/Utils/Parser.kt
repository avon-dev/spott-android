package com.avon.spott.Utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class Parser<T>(val type: T) {
    companion object {
        private val TAG = "Parser"

        fun <T> toJson(data: T): String {
//            val result = JSONObject().put("sending", GsonBuilder().create().toJson(data)).toString()
            val result = GsonBuilder().create().toJson(data).toString()
//            logd(TAG, "toJson(): $result")
            return result
        }

        inline fun <reified T> fromJson(payload: String): T {
            // 객체 json을 받을 때
//            return GsonBuilder().create().fromJson(payload, object : TypeToken<T>() {}.type)
            // payload 파싱된 문자열로 받을 때
            return GsonBuilder().create().fromJson(JSONObject(payload).getString("payload"), object : TypeToken<T>() {}.type)
//             // JSONObject
//            return GsonBuilder().create().fromJson(payload.getString("payload"), object : TypeToken<T>() {}.type)
        }
    }
}