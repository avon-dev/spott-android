package com.avon.spott

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.main.MainActivity
import com.avon.spott.main.controlToobar
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_photo.view.*
import kotlinx.android.synthetic.main.toolbar.view.*


class PhotoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photo, container, false)

        ////임시 더미 데이터/////////////////////////////
        root.text_nickname_photo_f.text = "userNickName"
        root.text_like_photo_f.text = "10"
        root.text_caption_photo_f.text = "이 곳에 사진에 대한 간단한 설명과 해시태그들이 들어갑니다. 해시태그에 대한 정확한 기획은 아직 안했습니다."
        root.text_allcomment_photo_f.text = "댓글 5개 모두 보기"
        root.text_date_photo_f.text = "2019년 12월 12일"
        //////////////////////////////////////////////

        root.imgbtn_map_photo_f.setOnClickListener {
            findNavController().navigate(R.id.action_photoFragment_to_photomapFragment)
        }

        root.const_comment_photo_f.setOnClickListener {
            findNavController().navigate(R.id.action_photoFragment_to_commentFragment)
        }

        root.text_nickname_photo_f.setOnClickListener {
            findNavController().navigate(R.id.action_photoFragment_to_userFragment)
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }
}
