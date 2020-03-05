package com.avon.spott.Utils

import com.google.gson.JsonObject
import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RetrofitService {

    @FormUrlEncoded
    @POST
    fun signIn(@Url url: String, @Field("email") email: String, @Field("password") password: String): Observable<Response<JsonObject>>

    @FormUrlEncoded
    @POST
    fun post(@Header("Authorization") token :String, @Url url: String, @Field("sending") sending: String): Observable<Response<String>>

    @GET
    fun get(@Header("Authorization") token:String, @Url url:String):Observable<Response<String>>

    @GET
    fun get(@Header("Authorization") token :String, @Url url: String, @Query("sending") sending: String): Observable<Response<String>>

    @DELETE
    fun delete(@Header("Authorization") token :String, @Url url: String, @Query("sending") sending: String): Observable<Response<String>>

    @DELETE
    fun delete(@Header("Authorization") token :String, @Url url: String): Observable<Response<String>>

    @FormUrlEncoded
    @PATCH
    fun patch(@Header("Authorization") token :String, @Url url: String, @Field("sending") sending: String): Observable<Response<String>>

    @Multipart
    @POST
    fun postPhoto(@Header("Authorization") token :String,
                  @Url url: String,
                  @Part("sending") sending: String,
                  @Part imageFile : ArrayList<MultipartBody.Part>): Observable<Response<String>>

    @Multipart
    @PATCH
    fun patchPhoto(@Header("Authorization") token :String,
                  @Url url: String,
                  @Part profile_image : MultipartBody.Part): Observable<Response<String>>


    /**  토큰 테스트용!!!!! 임시 토큰 2020-02-04   */

    @POST
    fun postNonHeader(@Url url: String, @Query("sending") sending: String): Observable<Response<String>>

    @GET
    fun getNonToken(@Url url: String, @Query("sending") sending: String): Observable<Response<String>>

    @FormUrlEncoded
    @PATCH
    fun patchNonHeader(@Url url: String, @Field("sending") sending: String): Observable<Response<String>>


    @FormUrlEncoded
    @POST
    fun postFieldNonHeader(@Url url: String, @Field("sending") sending: String): Observable<Response<String>>

}
