package com.avon.spott.Camera

import android.os.Bundle
import android.view.KeyEvent
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val fragment = supportFragmentManager.fragments.get(0)
            if (fragment is CameraFragment) {
                fragment.volumeCapture()
            }
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
}