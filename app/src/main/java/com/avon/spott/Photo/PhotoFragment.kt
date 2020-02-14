package com.avon.spott.Photo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.EditCaption.EditCaptionActivity
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.Mypage.MypageFragment.Companion.mypageChange
import com.avon.spott.PhotoEnlargementActivity
import com.avon.spott.Scrap.ScrapFragment.Companion.scrapChange
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.android.synthetic.main.toolbar.view.*


class PhotoFragment : Fragment(), PhotoContract.View, View.OnClickListener {

    private val TAG = "forPhotoFragment"

    companion object{
        var captionChange = false
    }

    private lateinit var photoPresenter : PhotoPresenter
    override lateinit var presenter : PhotoContract.Presenter

    //photoMapFragment 넘겨줄 데이터
    private var photoLat:Double? = null
    private var photoLng:Double? = null
    private var postPhotoUrl:String? = null

    //photoCameraActivity(가명) 넘겨줄 데이터
    private var backPhotoUrl:String? = null

    //commentFragment 넘겨줄 데이터
    private var userPhoto:String? = null
    private var userNickName:String? = null
    private var caption:String? = null
    private var dateTime:String? = null

    //userFragment 넘겨줄 데이터
    private var userId:Int = 0

    private var likeProgressing : Boolean = false //좋아요 처리중 여부
    private var scrapProgressing : Boolean = false //스크랩 처리중 여부

    private var likeCount = 0

    private var myself = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photo, container, false)

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
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE

        checkbox_scrap_photo_f.setOnCheckedChangeListener { _, _ ->
            if(!scrapProgressing){
                scrapProgressing = true
                checkbox_scrap_photo_f.isClickable = false

                if(checkbox_scrap_photo_f.isChecked){
                    presenter.postScrap(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }else{
                    presenter.deleteScrap(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }
            }

        }

        checkbox_like_photo_f.setOnCheckedChangeListener { _, _ ->
            if(!likeProgressing){

                likeProgressing = true
                checkbox_like_photo_f.isClickable = false

                if(checkbox_like_photo_f.isChecked) {
                    presenter.postLike(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }else{
                    presenter.deleteLike(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
                }
            }

        }

        if(captionChange){
            captionChange = false
            presenter.getPhotoDetail(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
        }
    }

    fun init(){
        photoPresenter = PhotoPresenter(this)

        imgbtn_map_photo_f.setOnClickListener(this)
        imgbtn_camera_photo_f.setOnClickListener(this)
        const_comment_photo_f.setOnClickListener(this)
        text_nickname_photo_f.setOnClickListener(this)
        img_userphoto_photo_f.setOnClickListener(this)
        img_photo_photo_f.setOnClickListener(this)
        MainActivity.mToolbar.img_more_toolbar.setOnClickListener(this)

    }

    override fun showPhotoMapUi(lat:Float, lng:Float, photoUrl:String) {
        val bundle = Bundle()
        bundle.putFloat("photoLat", lat)
        bundle.putFloat("photoLng", lng)
        bundle.putString("photoUrl", photoUrl)
        findNavController().navigate(R.id.action_photoFragment_to_photomapFragment, bundle)
    }

    override fun showCommentUi() {
        val bundle = Bundle()
        bundle.putInt("photoId", arguments?.getInt("photoId")!!)
        if(userPhoto==null){
            userPhoto = ""
        }
        bundle.putString("userPhoto", userPhoto)
        bundle.putString("userNickname", userNickName)
        bundle.putString("photoCaption", caption)
        bundle.putString("photoDateTime", dateTime)
        bundle.putInt("userId", userId)
        findNavController().navigate(R.id.action_photoFragment_to_commentFragment, bundle)
    }

    override fun showUserUi() {
        val bundle = Bundle()
        bundle.putInt("userId", userId)
        findNavController().navigate(R.id.action_photoFragment_to_userFragment, bundle)
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
            R.id.img_userphoto_photo_f -> {presenter.openUser()}
            R.id.img_photo_photo_f -> {
                logd(TAG, "postPhotoUrl : "+postPhotoUrl)
                if(postPhotoUrl!=null){
                    presenter.openPhotoEnlargement(postPhotoUrl!!)
                }
            }

            R.id.img_more_toolbar -> {
                if(postPhotoUrl!= null){
                    if(myself){ //내가 쓴 글인 경우
                        val builder = AlertDialog.Builder(context)

                        val arrayList = ArrayList<String>()
                        arrayList.add(getString(R.string.edit))
                        arrayList.add(getString(R.string.delete))
                        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, arrayList)

                        val listener = object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                when(which){
                                    0 -> { // 편집 눌렀을 때
                                        presenter.openEditCaption()
                                    }
                                    1 -> { //삭제 눌렀을 때
                                        showDeleteDialog()
                                    }
                                }
                            }
                        }

                        builder.setAdapter(adapter, listener)
                        builder.show()

                    }else{ //내가 쓴 글이 아닌 경우

                        val builder = AlertDialog.Builder(context)

                        val arrayList = ArrayList<String>()
                        arrayList.add(getString(R.string.report))
                        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, arrayList)

                        val listener = object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                when(which){
                                    0 -> { // 신고 눌렀을 때
                                        /** 사진(게시글) 신고 처리하는 코드 넣어야함.*/
                                    }
                                }
                            }
                        }

                        builder.setAdapter(adapter, listener)
                        builder.show()

                    }
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
                                likeChecked:Boolean, scrapChecked:Boolean, myself:Boolean,
                                userId:Int,hasHash:Boolean){
        this.myself = myself

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

        if(!hasHash){
            text_caption_photo_f.text = caption
        }


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

        this.userPhoto = userPhoto
        this.userNickName = userNickName
        this.caption = caption
        this.dateTime = dateTime

        this.userId = userId

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

    override fun showEditCaptionUi() {
        val nextIntent = Intent(context, EditCaptionActivity::class.java)
        nextIntent.putExtra("caption", caption)
        nextIntent.putExtra("photoId", arguments?.getInt("photoId"))
        startActivity(nextIntent)
    }

    private fun showDeleteDialog(){  /// 다이얼로그 아래 마진 없애야함.
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(getString(R.string.text_warning_deleting_photo))

        builder.setPositiveButton(android.R.string.yes){_, _ ->
            presenter.deletePhoto(getString(R.string.baseurl), arguments?.getInt("photoId")!!)
        }
        builder.setNegativeButton(android.R.string.no) {_, _ ->
        }

        val  mAlertDialog =  builder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

    }

    override fun navigateUp(){
        mToolbar.img_back_toolbar.performClick()
    }

    override fun showNoPhotoDialog(){  /// 다이얼로그 아래 마진 없애야함.
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(getString(R.string.text_no_photo))
        builder.setCancelable(false)

        builder.setPositiveButton(android.R.string.yes){_, _ ->
            navigateUp()
        }

        val  mAlertDialog =  builder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

    }


    override fun setCaption(text:String, hashList:ArrayList<Array<Int>>){

        var spannableString = SpannableString(text)

//           val startList = ArrayList<Int>()
            for (hash in hashList) {
//                if(!startList.contains(hash[0])){
//                    startList.add(hash[0])

                    val start = hash[0]
                    val end = hash[1]


                    spannableString.setSpan(object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false

                        }
                        override fun onClick(widget: View) {
                            showToast(text.substring(start,end))
                        }
                    }, start, end,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

//                }

            }

        text_caption_photo_f.text = spannableString
        text_caption_photo_f.movementMethod = LinkMovementMethod.getInstance()

    }



}
