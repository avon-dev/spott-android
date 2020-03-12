package com.avon.spott.Utils

import android.app.Application
import android.content.Context
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class App : Application(), CameraXConfig.Provider {

    companion object {
        lateinit var prefs : MySharedPreferences
        lateinit var mContext: Context

        val SERVER_ERROR_400 = 400 // Bad Request
        val SERVER_ERROR_404 = 404 // Not Found
        val SERVER_ERROR_500 = 500 // Server Internal Error
        val ERROR_ERTRY = 499
        val ERROR_PUBLICKEY = 498
    }
    /* prefs라는 이름의 MySharedPreferences 하나만 생성할 수 있도록 설정. */

    override fun onCreate() {
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()
        mContext = this
    }

    fun getContext(): Context {
        return mContext
    }

    // CameraXConfig.Provider : CameraX에 대한 구성을 제공하기 위해 구현될 수 있는 인터페이스.
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}