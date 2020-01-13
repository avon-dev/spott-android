package com.avon.spott.Utils

import android.media.ExifInterface
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

class ExifExtractor {
    companion object {

        private val TAG = "ExifExtractor"

        fun extractLatLng(photoPath: String): LatLng? {
            try {
                val exifInterface = ExifInterface(photoPath)
                val latLng = FloatArray(2)
                val photoLatLng: LatLng
                if (exifInterface.getLatLong(latLng)) {
                    photoLatLng = LatLng(latLng.get(0).toDouble(), latLng.get(1).toDouble())
                } else {
                    //사진의 위치 정보가 없으면 구글앱 카메라 서울시청
                    photoLatLng = LatLng(37.56668, 126.97843)
                }

                return photoLatLng

            } catch (e: IOException) {
                logd(TAG, "exifInteface IOException : " + e.localizedMessage)
                return null
            }
        }
    }

}