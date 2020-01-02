package com.avon.spott.Utils

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class Parser<T>(val type: T) {
    companion object {
        fun <T> toJson(data: T): String {
            return JSONObject().put("sending", GsonBuilder().create().toJson(data)).toString()
        }

        inline fun <reified T> fromJson(payload: String): T {
            return GsonBuilder().create()
                .fromJson(JSONObject(payload).getString("payload"), object : TypeToken<T>() {}.type)
        }
    }
}