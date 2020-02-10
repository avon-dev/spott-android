package com.avon.spott

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_photo_enlargement.*

class PhotoEnlargementActivity  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_enlargement)

        val builder = Zoomy.Builder(this).target(img_photo_photodetail_a)
        builder.register()

        Glide.with(this)
            .load(intent.getStringExtra("photoUrl"))
            .into(img_photo_photodetail_a)

        imgbtn_close_photodetail_a.setOnClickListener { onBackPressed() }
    }
}