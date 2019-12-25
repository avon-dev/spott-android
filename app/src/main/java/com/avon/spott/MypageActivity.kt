package com.avon.spott

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_mypage.*
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

        floatingbtn_addphoto_mypage.setOnClickListener {
            val nextIntent = Intent(this, AddPhotoActivity::class.java)
            startActivity(nextIntent)
        }

        val topButtonsListner = View.OnClickListener {
            if(it.id == R.id.imgbtn_grid_mypage_f) {
                imgbtn_grid_mypage_f.isSelected= true
                imgbtn_map_mypage_f.isSelected = false
            }else{
                imgbtn_grid_mypage_f.isSelected=false
                imgbtn_map_mypage_f.isSelected = true
            }
        }

        imgbtn_grid_mypage_f.setOnClickListener(topButtonsListner)
        imgbtn_map_mypage_f.setOnClickListener(topButtonsListner)
        imgbtn_grid_mypage_f.performClick()
    }
}
