package com.avon.spott.Photo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import com.avon.spott.PhotoEnlargementActivity
import com.avon.spott.Scrap.ScrapFragment.Companion.scrapChange
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlin.math.log


class PhotoFragment : Fragment(), PhotoContract.View, View.OnClickListener {

    private val TAG = "forPhotoFragment"

    private lateinit var photoPresenter : PhotoPresenter
    override lateinit var presenter : PhotoContract.Presenter

    private var photoLat:Double? = null
    private var photoLng:Double? = null
    private var postPhotoUrl:String? = null

    private var backPhotoUrl:String? = null

    private var likeProgressing : Boolean = false //좋아요 처리중 여부
    private var scrapProgressing : Boolean = false //스크랩 처리중 여부

    private var likeCount = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photo, container, false)

        //주미 라이브러리 쓰면 이미지뷰 클릭 안됨.
//        val builder = Zoomy.Builder(activity).target(root.img_photo_photo_f)
//        builder.register()

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //넘어오는 photoId값
        val photoId = arguments?.getInt("photoId")
        showToast(photoId.toString())

        checkbox_like_photo_f.isClickable = false
        likeProgressing = true
        scrapProgressing = true
        presenter.getPhotoDetail(getString(R.string.baseurl), photoId!!)


    }

    override fun onStart() {
        super.onStart()

        //툴바 처리 (뒤로가기 + 더보기)
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE


        //스크랩, 좋아요 체크박스 눌렀을 때 뷰 등장하는 애니메이션 효과
//        val animation = AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox)
        checkbox_scrap_photo_f.setOnCheckedChangeListener { buttonView, isChecked ->
            if(!scrapProgressing){
                scrapProgressing = true
                checkbox_scrap_photo_f.isClickable = false

//                checkbox_scrap_photo_f.startAnimation(animation)

                if(checkbox_scrap_photo_f.isChecked){
                    presenter.postScrap(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }else{
                    presenter.deleteScrap(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }
            }

        }

        checkbox_like_photo_f.setOnCheckedChangeListener { buttonView, isChecked ->
            if(!likeProgressing){

                likeProgressing = true
                checkbox_like_photo_f.isClickable = false

//                checkbox_like_photo_f.startAnimation(animation)

                if(checkbox_like_photo_f.isChecked) {
                    presenter.postLike(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }else{
                    presenter.deleteLike(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }
            }

        }
    }

    fun init(){
        photoPresenter = PhotoPresenter(this)

        imgbtn_map_photo_f.setOnClickListener(this)
        imgbtn_camera_photo_f.setOnClickListener(this)
        const_comment_photo_f.setOnClickListener(this)
        text_nickname_photo_f.setOnClickListener(this)
        img_photo_photo_f.setOnClickListener(this)

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

    override fun showPhotoEnlagement(photoUrl: String) {
        val nextIntent = Intent(context!!, PhotoEnlargementActivity::class.java)
        nextIntent.putExtra("photoUrl", photoUrl)
        startActivity(nextIntent)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgbtn_map_photo_f -> {
                if(photoLat!=null){
                    presenter.openPhotoMap(photoLat!!, photoLng!!, postPhotoUrl!!)
                }
            }
            R.id.imgbtn_camera_photo_f->{
                if(backPhotoUrl!=null){
                    showToast(backPhotoUrl!!)   //임시데이터, 나중에 카메라액티비티로 연결되게 바꿔야함
                }
            }
            R.id.const_comment_photo_f -> {presenter.openComment()}
            R.id.text_nickname_photo_f -> {presenter.openUser()}
            R.id.img_photo_photo_f -> {
                logd(TAG, "postPhotoUrl : "+postPhotoUrl)
                if(postPhotoUrl!=null){
                    presenter.openPhotoEnlargement(postPhotoUrl!!)
                }
            }

        }
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun setPhotoDetail(userPhoto:String?, userNickName:String, postPhotoUrl: String,
                                backPhotoUrl:String, photoLat:Double, photoLng:Double,
                                caption:String, comments:Int, dateTime:String, likeCount:Int,
                                likeChecked:Boolean, scrapChecked:Boolean){

        if(userPhoto==null){
            logd(TAG,"null")
            img_userphoto_photo_f.setImageResource(R.drawable.ic_account_circle_grey_36dp)
        }else{
            Glide.with(context!!)
                .load(userPhoto)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_userphoto_photo_f)
        }

        text_like_photo_f.text = likeCount.toString()
        this.likeCount = likeCount

            text_nickname_photo_f.text = userNickName

        Glide.with(context!!)
            .load(postPhotoUrl)
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(img_photo_photo_f)

        text_caption_photo_f.text = caption

        if(comments==0){
            text_allcomment_photo_f.text = "댓글 쓰기"
        }else{
            text_allcomment_photo_f.text = "댓글 "+comments+"개 모두보기"
        }

        text_date_photo_f.text = dateTime

        this.photoLat = photoLat
        this.photoLng = photoLng
        this.postPhotoUrl = postPhotoUrl
        this.backPhotoUrl = backPhotoUrl


        checkbox_like_photo_f.isChecked = likeChecked
        checkbox_scrap_photo_f.isChecked = scrapChecked

        checkbox_like_photo_f.isClickable = true
        checkbox_scrap_photo_f.isClickable = true

        likeProgressing = false
        scrapProgressing = false

    }

    override fun likeResultDone(count:Int){

        logd(TAG, "count : "+count)

        logd(TAG, "likeCount1 : "+likeCount)

        likeCount = likeCount + count

        logd(TAG, "likeCount2 : "+likeCount)

        text_like_photo_f.text = likeCount.toString()

        checkbox_like_photo_f.startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox))

        likeProgressing = false
        checkbox_like_photo_f.isClickable = true
    }

    override fun likeResultError() {
        if(checkbox_like_photo_f.isChecked){
            checkbox_like_photo_f.isChecked = false
        }else{
            checkbox_like_photo_f.isChecked = true
        }
        likeProgressing = false
        checkbox_like_photo_f.isClickable= true
    }


    override fun scrapResultDone(){
        checkbox_scrap_photo_f.startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox))

        scrapProgressing = false
        checkbox_scrap_photo_f.isClickable = true

        scrapChange = true
    }

    override fun scrapResultError(){
        if(checkbox_scrap_photo_f.isChecked){
            checkbox_scrap_photo_f.isChecked = false
        }else{
            checkbox_scrap_photo_f.isChecked = true
        }
        scrapProgressing = false
        checkbox_scrap_photo_f.isClickable = true
    }




}
