package com.avon.spott.Utils

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context: Context) {

    val PREFS_FILENAME = "pref"
    val PREF_KEY_POSITION_LAT = "mylastlat"
    val PREF_KEY_POSITION_LNG = "mylastlng"
    val PREF_KEY_POSITION_ZOOM = "mylastzoom"
    val  PREF_KEY_CAMERA_GUIDE = "camera_guide"
    private val PREF_ACCESS_TOKEN = "access"
    private val PREF_REFRESH_TOKEN = "refresh"

    /**  토큰 테스트용!!!!! 임시 토큰 2020-02-04   */
//    val PRED_KEY_TEST_FOR_TOKEN = "temporary_token"

    // 파일 이름과 데이터를 저장할 Key 값을 만들고 prefs 인스턴스 초기화
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    //  get()은 데이터 가져옴.
    //  set(value) 실행 시 value로 값을 대체한 후 저장

    var mylastlat: Float //마지막 위도값
        get() = prefs.getFloat(PREF_KEY_POSITION_LAT, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_LAT, value).apply()

    var mylastlng: Float //마지막 경도값
        get() = prefs.getFloat(PREF_KEY_POSITION_LNG, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_LNG, value).apply()

    var mylastzoom: Float //마지막 확대값, 줌값
        get() = prefs.getFloat(PREF_KEY_POSITION_ZOOM, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_ZOOM, value).apply()

    var camera_guide:Boolean // 카메라 윤곽선 버튼 가이드 보여준 적이 있는지
        get() = prefs.getBoolean(PREF_KEY_CAMERA_GUIDE, false)
        set(value) = prefs.edit().putBoolean(PREF_KEY_CAMERA_GUIDE, value).apply()

    /**  토큰 테스트용!!!!! 임시 토큰 2020-02-04   */
//    var temporary_token:String //임시 토큰
//        get() = prefs.getString(PRED_KEY_TEST_FOR_TOKEN, "")
//        set(value) = prefs.edit().putString(PRED_KEY_TEST_FOR_TOKEN, value).apply()

    var token: String
        get() = prefs.getString(PREF_ACCESS_TOKEN, "")
        set(value) =  prefs.edit().putString(PREF_ACCESS_TOKEN, value).apply()
    var refresh: String
        get() = prefs.getString(PREF_REFRESH_TOKEN, "")
        set(value) = prefs.edit().putString(PREF_REFRESH_TOKEN, value).apply()

    fun deleteToken() {
        val edit = prefs.edit()
        edit.clear()
        edit.commit()
    }
}