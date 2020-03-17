package com.avon.spott.Utils

import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class Retrofit(baseUrl:String) {
    private val retrofitService =
        createRetrofit(baseUrl).create(RetrofitService::class.java)

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    fun signIn(url:String, email:String, password:String): Observable<Response<JsonObject>> {
        return retrofitService.signIn(url, email, password)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun signIn(url:String, email:String, password:String, usertype:Int): Observable<Response<JsonObject>> {
        return retrofitService.signIn(url, email, password, usertype)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun signIn(url:String, email:String, usertype:Int): Observable<Response<JsonObject>> {
        return retrofitService.signIn(url, email, usertype)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun get(token:String, url:String): Observable<Response<String>> {
        return retrofitService.get("jwt "+token, url)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun get(token: String, url: String, sending: String): Observable<Response<String>> {
        return retrofitService.get("jwt "+token, url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun post(token: String, url: String, sending: String): Observable<Response<String>> {
        return retrofitService.post("jwt "+token, url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(token: String, url:String): Observable<Response<String>> {
        return retrofitService.delete("jwt "+token, url)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun delete(token: String, url: String, sending: String): Observable<Response<String>> {
        return retrofitService.delete("jwt "+token, url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun patch(token: String, url: String, sending: String): Observable<Response<String>> {
        return retrofitService.patch("jwt "+token, url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun postPhoto(token: String, url:String, sending: String, imageFile : ArrayList<MultipartBody.Part>)
            : Observable<Response<String>> {
        return retrofitService.postPhoto("jwt "+token, url, sending, imageFile)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun patchPhoto(token:String, url:String, imageFile:MultipartBody.Part) : Observable<Response<String>> {
        return retrofitService.patchPhoto("jwt "+token, url, imageFile)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**  토큰 테스트용!!!!! 임시 토큰 2020-02-04   */
    fun postNonHeader(url: String, sending: String): Observable<Response<String>> {
        return retrofitService.postNonHeader(url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun postNonHeader(url: String, key: String, iv:String, pw:String): Observable<Response<String>> {
        return retrofitService.postNonHeader(url, key, iv)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNonHeader(url: String, email: String, usertype:Int): Observable<Response<String>> {
        return retrofitService.getNonToken(url, email, usertype)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNonHeader(url: String, sending: String): Observable<Response<String>> {
        return retrofitService.getNonToken(url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNonHeader(url: String): Observable<Response<String>> {
        return retrofitService.getNonToken(url)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun patchNonHeader(url:String, sending:String): Observable<Response<String>> {
        return retrofitService.patchNonHeader(url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun postFieldNonHeader(url: String, sending: String): Observable<Response<String>> {
        return retrofitService.postFieldNonHeader(url, sending)
            .subscribeOn(Schedulers.io())
            .map { t -> if (t.isSuccessful) t else throw HttpException(t) }
            .observeOn(AndroidSchedulers.mainThread())
    }
}