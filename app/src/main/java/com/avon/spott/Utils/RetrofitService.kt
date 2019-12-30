package com.avon.spott.Utils

import com.avon.spott.Data.Token
import com.google.gson.JsonObject
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// https://gist.github.com/poqw/139310db9f33505bc845e16608ebe1fe

interface RetrofitService {
    @FormUrlEncoded
    @POST("/api/token/")
    fun testLogin(@Field("email") email: String, @Field("password") password: String): Observable<Token>

    @GET("/api/testapp/login/")
    fun testLogin2(@Header("Authorization") Autorization: String): Observable<JsonObject>

    companion object {
        fun create(): RetrofitService {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("")
                .build()

            return retrofit.create(RetrofitService::class.java)
        }
    }
}
