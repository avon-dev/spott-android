package com.avon.spott.Camera

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

// CameraXConfig.Provider를 구현한다.
class CameraXBasic : Application(), CameraXConfig.Provider {
    /*
 CameraXConfig.Provider : CameraX에 대한 구성을 제공하기 위해 구현될 수 있는 인터페이스.
*/
    // Camera2 defaultConfig 반환
    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}