package com.avon.spott

import android.widget.Toast
import com.avon.spott.Camera.CameraActivity

fun CameraActivity.showToast(text:String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}