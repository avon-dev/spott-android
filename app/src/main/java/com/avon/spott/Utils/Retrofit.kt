package com.avon.spott.Utils

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NetRetrofit {
    private val retrofit = Retrofit.Builder()
        .baseUrl("")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getService(): RetrofitService {
        return retrofit.create(RetrofitService::class.java)
    }
}