package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.toolbar.*

class MypageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        img_noti_toolbar.visibility = View.VISIBLE
        img_right_toolbar.visibility = View.VISIBLE
        img_profile_toolbar.visibility = View.VISIBLE
        text_name_toolbar.visibility = View.VISIBLE

        img_back_toolbar.setOnClickListener {
            onBackPressed()
        }
    }
}
