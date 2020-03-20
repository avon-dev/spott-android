package com.avon.spott.Photo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.Camera.CameraXActivity
import com.avon.spott.EditCaption.EditCaptionActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.PhotoEnlargementActivity
import com.avon.spott.R
import com.avon.spott.Scrap.ScrapFragment.Companion.scrapChange
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_report.view.*
import kotlinx.android.synthetic.main.dialog_report_etc.view.*
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

    private var showdetail = false

    private var dialog : AlertDialog? = null

    private var postKind = 200

    private var isSuperuser = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photo, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.GONE,View.GONE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE


        //넘어오는 photoId값
        val photoId = arguments?.getInt("photoId")
//        showToast(photoId.toString())

        checkbox_like_photo_f.isClickable = false
        likeProgressing = true
        scrapProgressing = true
        presenter.getPhotoDetail(getString(R.string.baseurl), photoId!!)

    }

    override fun onStart() {
        super.onStart()

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

        scroll_photo_f.visibility = if(showdetail) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        if(dialog!=null && dialog!!.isShowing)
            dialog!!.dismiss()

        super.onDestroy()

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
//        bundle.putInt("photoId", arguments?.getInt("photoId")!!)
        findNavController().navigate(R.id.action_photoFragment_to_commentFragment, bundle)
    }

    override fun showUserUi() {
        val bundle = Bundle()
        bundle.putInt("userId", userId)
        findNavController().navigate(R.id.action_photoFragment_to_userFragment, bundle)
    }

    override fun showPhotoEnlagementUi(photoUrl: String) {
        val nextIntent = Intent(context!!, PhotoEnlargementActivity::class.java)
        nextIntent.putExtra("photoUrl", photoUrl)
        nextIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(nextIntent)
    }

    override fun showHashtagUi(hashtag:String){
        val bundle = Bundle()
        bundle.putString("hashtag", hashtag)
        findNavController().navigate(R.id.action_photoFragment_to_hashtagFragment, bundle)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgbtn_map_photo_f -> {
                if(photoLat!=null){
                    presenter.openPhotoMap(photoLat!!, photoLng!!, postPhotoUrl!!)
                }
            }
            R.id.imgbtn_camera_photo_f->{
                postPhotoUrl?.let { presenter.openCamera(it, backPhotoUrl) }

                /* // 수정
                if(backPhotoUrl!=null){
                    presenter.openCamera(backPhotoUrl!!)
                }
                */
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
                        this.dialog = builder.show()

                    }else{ //내가 쓴 글이 아닌 경우

                        val builder = AlertDialog.Builder(context)

                        val arrayList = ArrayList<String>()
                        arrayList.add(getString(R.string.report))
                        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, arrayList)

                        val listener = object :DialogInterface.OnClickListener{
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                when(which){
                                    0 -> { // 신고 눌렀을 때
                                       report()
                                    }
                                }
                            }
                        }

                        builder.setAdapter(adapter, listener)

                        this.dialog = builder.show()

                    }
                }
            }

        }
    }

    private fun report(){
        val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.dialog_report, null)
        //AlertDialogBuilder

        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView)

        mDialogView.text_header_report_d.text = getString(R.string.report_photo)

        val  mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.radiogroup_report_d.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                mDialogView.btn_report_d.isClickable = true
                mDialogView.btn_report_d.setBackgroundResource(R.drawable.corner_round_primary)
            }
        )

        mDialogView.btn_report_d.setOnClickListener {
            val radioId = mDialogView.radiogroup_report_d.checkedRadioButtonId
            val checkedRadio = mDialogView.radiogroup_report_d.findViewById<View>(radioId)
            val index = mDialogView.radiogroup_report_d.indexOfChild(checkedRadio)

            val detail:String
           when(index){
               0 ->{ detail = getString(R.string.spam) }
               1 ->{ detail = getString(R.string.abuse_and_slander)}
               2 -> {detail = getString(R.string.pornography)}
               3 -> {detail = getString(R.string.unauthorized_use)}
               else -> {detail = ""}
           }



            presenter.report(getString(R.string.baseurl), index+1, detail,
                arguments?.getInt("photoId")!!, postPhotoUrl!!, caption!!, mAlertDialog )
            mAlertDialog.dismiss()
        }

        mDialogView.text_etc_report_d.setOnClickListener {
            mAlertDialog.dismiss()
            reportEtc()
        }
    }

    private fun reportEtc(){
        val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.dialog_report_etc, null)
        //AlertDialogBuilder

        val mBuilder = AlertDialog.Builder(context!!)
            .setView(mDialogView)

        mDialogView.text_header_reportetc_d.text = getString(R.string.report_photo)

        val  mAlertDialog = mBuilder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

        mDialogView.edit_reportetc_d.addTextChangedListener {
            if(it!!.trim().length>0){
                mDialogView.btn_reportetc_d.isClickable = true
                mDialogView.btn_reportetc_d.setBackgroundResource(R.drawable.corner_round_primary)
            }
        }

        mDialogView.btn_reportetc_d.setOnClickListener {
            presenter.report(getString(R.string.baseurl), 0, mDialogView.edit_reportetc_d.text.toString(),
                arguments?.getInt("photoId")!!, postPhotoUrl!!, caption!!, mAlertDialog)

        }

    }

    override fun serverError(){
        showToast(getString(R.string.server_connection_error))
    }

    override fun reportDone(alertDialog: AlertDialog){
        showToast(getString(R.string.report_done))
        alertDialog.dismiss()
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun setPhotoDetail(userPhoto:String?, userNickName:String, postPhotoUrl: String,
                                backPhotoUrl:String?, photoLat:Double, photoLng:Double,
                                caption:String, comments:Int, dateTime:String, likeCount:Int,
                                likeChecked:Boolean, scrapChecked:Boolean, myself:Boolean,
                                userId:Int,hasHash:Boolean, postKind:Int, isSuperuser:Boolean){

        this.postKind = postKind

        this.isSuperuser = isSuperuser


        if(postKind==200){ //일반 게시물일 때 툴바 처리 (뒤로가기 + 더보기)
            controlToolbar(View.VISIBLE, View.GONE, View.GONE,View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
            MainActivity.mToolbar.visibility = View.VISIBLE
        }else if(postKind==201){
            if(this.isSuperuser){  // 포포 추천 게시물이고 관리자 계정일때 툴바 처리(뒤로가기+제목+더보기)
                controlToolbar(View.VISIBLE, View.GONE, View.GONE,View.GONE, View.VISIBLE,View.VISIBLE, View.GONE, View.GONE, View.GONE)
                MainActivity.mToolbar.visibility = View.VISIBLE
            }else{  // 포포 추천 게시물일 때 툴바 처리(뒤로가기+제목)
                controlToolbar(View.VISIBLE, View.GONE, View.GONE,View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
                MainActivity.mToolbar.visibility = View.VISIBLE
            }

        }


        showdetail = true

        this.myself = myself

        text_like_photo_f.text = likeCount.toString()
        this.likeCount = likeCount

        text_nickname_photo_f.text = userNickName

        if(userPhoto==null){
            logd(TAG,"null")
            img_userphoto_photo_f.setImageResource(R.drawable.img_person)
        }else{
            Glide.with(context!!)
                .load(userPhoto)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_userphoto_photo_f)
        }

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

        scroll_photo_f.visibility = View.VISIBLE


    }

    override fun likeResultDone(count:Int){

        logd(TAG, "count : "+count)

        logd(TAG, "likeCount1 : "+likeCount)

        logd(TAG, "likeCount2 : "+likeCount)

        if(text_like_photo_f != null && checkbox_like_photo_f !=null){
            likeCount = likeCount + count
            text_like_photo_f.text = likeCount.toString()

            checkbox_like_photo_f.startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox))
            checkbox_like_photo_f.isClickable = true
        }


        likeProgressing = false

    }

    override fun likeResultError() {
        if(checkbox_like_photo_f !=null){
            if(checkbox_like_photo_f.isChecked){
                checkbox_like_photo_f.isChecked = false
            }else{
                checkbox_like_photo_f.isChecked = true
            }
            checkbox_like_photo_f.isClickable= true
        }

        likeProgressing = false

    }


    override fun scrapResultDone(){
        if(checkbox_scrap_photo_f!=null){
            checkbox_scrap_photo_f.startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.scale_checkbox))
            checkbox_scrap_photo_f.isClickable = true
        }

        scrapProgressing = false
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

    override fun showCameraUi(postPhotoUrl: String, backPhotoUrl: String?) {
//        showToast(photoUrl)
        /**
         * 여기에 카메라 연결하는 코드 넣으면 됨!!!!!
         * scrapitem 클래스로 변경하기
         *                                    */

        Intent(context, CameraXActivity::class.java).let {
            it.putExtra("postPhotoUrl", postPhotoUrl)
            it.putExtra("backPhotoUrl", backPhotoUrl)
            startActivity(it)
        }
    }

    override fun navigateUp(){
        mToolbar.img_back_toolbar.performClick()
    }

    override fun showNoPhotoDialog(){  /// 다이얼로그 아래 마진 없애야함.

        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(getString(R.string.text_no_photo))
        builder.setCancelable(false) //뒤로가기로 다이얼로그 종료 방지

        builder.setPositiveButton(android.R.string.yes){_, _ ->
            navigateUp()
        }

        val  mAlertDialog =  builder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)

    }


    override fun setCaption(text:String, hashList:ArrayList<Array<Int>>){

        var spannableString = SpannableString(text)

            for (hash in hashList) {

                    val start = hash[0]
                    val end = hash[1]


                    spannableString.setSpan(object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false

                        }
                        override fun onClick(widget: View) {
                            presenter.openHashtag(text.substring(start,end))
                        }
                    }, start, end,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            }

        text_caption_photo_f.text = spannableString
        text_caption_photo_f.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun showReportedDialog() {

        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(getString(R.string.text_reported_photo))
        builder.setCancelable(false) //뒤로가기로 다이얼로그 종료 방지

        builder.setPositiveButton(android.R.string.yes){_, _ ->
            navigateUp()
        }

        val  mAlertDialog =  builder.show()
        mAlertDialog.setCanceledOnTouchOutside(false)
    }

    override fun photoDeleteError() {
        showToast(getString(R.string.toast_photo_delete_error))
    }







}