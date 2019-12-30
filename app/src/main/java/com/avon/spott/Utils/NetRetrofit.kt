package com.avon.spott.Utils

import android.util.Log
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NetRetrofit {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://15.164.213.66:8000")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getService(): RetrofitService {
        Log.d("TestLogin","getService()")
        return retrofit.create(RetrofitService::class.java)
    }
}