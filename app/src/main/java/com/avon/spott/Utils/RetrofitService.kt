package com.avon.spott.Utils

import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {
    @FormUrlEncoded
    @POST
    fun signIn(@Url url: String, @Field("email") email: String, @Field("password") password: String): Observable<Response<String>>

    @FormUrlEncoded
    @POST
    fun post(@Url url: String, @Field("sending") sending: String): Observable<Response<String>>


    @GET
    fun get(@Url url: String, @Query("sending") sending: String): Observable<Response<String>>
}
