package com.avon.spott.Photo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ablanco.zoomy.Zoomy
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.android.synthetic.main.fragment_photo.view.*


class PhotoFragment : Fragment(), PhotoContract.View, View.OnClickListener {

    private lateinit var photoPresenter : PhotoPresenter
    override lateinit var presenter : PhotoContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photo, container, false)

        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ임시 더미 데이터ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
        Glide.with(this)
            .load("https://cdn.pixabay.com/photo/2018/01/15/14/07/background-3084012_960_720.jpg")
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(root.img_photo_photo_f)

        root.text_nickname_photo_f.text = "userNickName"
        root.text_like_photo_f.text = "10"
        root.text_caption_photo_f.text = "이 곳에 사진에 대한 간단한 설명과 해시태그들이 들어갑니다. 해시태그에 대한 정확한 기획은 아직 안했습니다. 이 곳에 사진에 대한 간단한 설명과 해시태그들이 들어갑니다. 해시태그에 대한 정확한 기획은 아직 안했습니다."
        root.text_allcomment_photo_f.text = "댓글 5개 모두 보기"
        root.text_date_photo_f.text = "2019년 12월 12일"

        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

        //------넘어오는 photoId값 확인------------
        val photoId = arguments?.getInt("photoId")
        showToast(photoId.toString())
        //--------------------------------------

        val builder = Zoomy.Builder(activity).target(root.img_photo_photo_f)
        builder.register()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()

        //툴바 처리 (뒤로가기 + 더보기)
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE


        //스크랩, 좋아요 체크박스 눌렀을 때 뷰 등장하는 애니메이션 효과
        val animation = AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox)
        checkbox_scrap_photo_f.setOnCheckedChangeListener { buttonView, isChecked ->
            checkbox_scrap_photo_f.startAnimation(animation)
        }
        checkbox_like_photo_f.setOnCheckedChangeListener { buttonView, isChecked ->
            checkbox_like_photo_f.startAnimation(animation)
        }
    }

    fun init(){
        photoPresenter = PhotoPresenter(this)

        imgbtn_map_photo_f.setOnClickListener(this)
        const_comment_photo_f.setOnClickListener(this)
        text_nickname_photo_f.setOnClickListener(this)
    }

    override fun showPhotoMapUi(lat:Float, lng:Float, photoUrl:String) {
        val bundle = Bundle()
        bundle.putFloat("photoLat", lat)
        bundle.putFloat("photoLng", lng)
        bundle.putString("photoUrl", photoUrl)
        findNavController().navigate(R.id.action_photoFragment_to_photomapFragment, bundle)
    }

    override fun showCommentUi() {
        findNavController().navigate(R.id.action_photoFragment_to_commentFragment)
    }

    override fun showUserUi() {
        findNavController().navigate(R.id.action_photoFragment_to_userFragment)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgbtn_map_photo_f -> {presenter.openPhotoMap(
                37.521655, 126.924161,
                "https://cdn.pixabay.com/photo/2018/01/15/14/07/background-3084012_960_720.jpg")} //임시데이터, 나중에 이 사진의 위도, 경도, 사진url 로 바꿔야함.
            R.id.const_comment_photo_f -> {presenter.openComment()}
            R.id.text_nickname_photo_f -> {presenter.openUser()}

        }
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }


}
