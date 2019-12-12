package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar.*

class PhotoMapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_map)

        text_main_toolbar.text = "지도"

        img_back_toolbar.setOnClickListener {
            onBackPressed()
        }
    }
}
