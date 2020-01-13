package com.avon.spott.Utils

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences (context: Context) {

    val PREFS_FILENAME = "pref"
    val PREF_KEY_POSITION_LAT = "mylastlat"
    val PREF_KEY_POSITION_LNG = "mylastlng"
    val PREF_KEY_POSITION_ZOOM = "mylastzoom"

    // 파일 이름과 데이터를 저장할 Key 값을 만들고 prefs 인스턴스 초기화
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    //  get()은 데이터 가져옴.
    //  set(value) 실행 시 value로 값을 대체한 후 저장

    var mylastlat: Float //마지막 위도값
        get() = prefs.getFloat(PREF_KEY_POSITION_LAT, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_LAT,  value).apply()

    var mylastlng: Float //마지막 경도값
        get() = prefs.getFloat(PREF_KEY_POSITION_LNG, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_LNG,  value).apply()

    var mylastzoom: Float //마지막 확대값, 줌값
        get() = prefs.getFloat(PREF_KEY_POSITION_ZOOM, 0.0f)
        set(value) = prefs.edit().putFloat(PREF_KEY_POSITION_ZOOM,  value).apply()
}