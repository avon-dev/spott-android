package com.avon.spott

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_mypage.*


class MypageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mypage, container, false)

        floatingbtn_addphoto_mypage.setOnClickListener {
//            val nextIntent = Intent(this, AddPhotoActivity::class.java)
//            startActivity(nextIntent)
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





