package com.avon.spott

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.android.synthetic.main.toolbar.*

class PhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        img_back_toolbar.setOnClickListener {
            onBackPressed()
        }

        img_right_toolbar.visibility = View.VISIBLE

        img_right_toolbar.setOnClickListener {
            val nextIntent = Intent(this, MypageActivity::class.java)
            startActivity(nextIntent)
        }

        imgbtn_map_photo_f.setOnClickListener {
            val nextIntent = Intent(this, PhotoMapActivity::class.java)
            startActivity(nextIntent)
        }
    }
}
