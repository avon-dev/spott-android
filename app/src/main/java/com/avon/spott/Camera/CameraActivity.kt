package com.avon.spott.Camera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.CameraFragment
import com.avon.spott.R

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.frame_surface_camera_a, CameraFragment.newInstance())
            .commit()
    }
}