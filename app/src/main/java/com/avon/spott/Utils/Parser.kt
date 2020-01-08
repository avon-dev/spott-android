package com.avon.spott.Utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class Parser<T>(val type: T) {
    companion object {
        private val TAG = "Parser"

        fun <T> toJson(data: T): String {
            return GsonBuilder().create().toJson(data).toString()
        }

        inline fun <reified T> fromJson(payload: String): T {
            return GsonBuilder().create().fromJson(JSONObject(payload).getString("payload"), object : TypeToken<T>() {}.type)
        }
    }
}