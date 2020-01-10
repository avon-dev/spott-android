package com.avon.spott

import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import com.avon.spott.Utils.loge
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

internal class ImageSaver(
    private val image: Image,
    private val file: File
) : Runnable {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
        } catch (e: IOException) {
            loge(TAG, e.toString())
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    loge(TAG, e.toString())
                }
            }
        }
    }

    companion object {
        private val TAG = "ImageSaver"
    }
}