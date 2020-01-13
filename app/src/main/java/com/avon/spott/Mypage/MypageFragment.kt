package com.avon.spott.Mypage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.AddPhoto.AddPhotoActivity
import com.avon.spott.EditMyinfo.EditMyInfoActivity
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToobar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MypageFragment : Fragment(), MypageContract.View, View.OnClickListener {

    private val TAG = "MypageFragment"

    private val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage.jpg"

    var Mypageselectgrid = true

    private lateinit var mypagePresenter: MypagePresenter
    override lateinit var presenter: MypageContract.Presenter

    val mypageInterListener = object : mypageInter{
        override fun itemClick(){
            presenter.openPhoto()
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_mypage, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //---------------리사이클러뷰테스트 코드------------------------------
        val layoutManager = GridLayoutManager(context!!, 3)

        recycler_grid_mypage_f.layoutManager  = layoutManager
        recycler_grid_mypage_f.adapter = MypageAdapter(context!!, mypageInterListener)
        //-----------------------------------------------------------------



        ////////마이페이지 뷰 선택 --- 일단 나중에
        val topButtonsListner = View.OnClickListener {
            if(it.id == R.id.imgbtn_grid_mypage_f) {
                imgbtn_grid_mypage_f.isSelected= true
                imgbtn_map_mypage_f.isSelected = false
                const_grid_mypage_f.visibility = View.VISIBLE
                const_map_mypage_f.visibility = View.GONE
                Mypageselectgrid = true
            }else{
                imgbtn_grid_mypage_f.isSelected=false
                imgbtn_map_mypage_f.isSelected = true
                const_grid_mypage_f.visibility = View.GONE
                const_map_mypage_f.visibility = View.VISIBLE
                Mypageselectgrid = false
            }
        }

        imgbtn_grid_mypage_f.setOnClickListener(topButtonsListner)
        imgbtn_map_mypage_f.setOnClickListener(topButtonsListner)

        if(Mypageselectgrid){
            imgbtn_grid_mypage_f.performClick()
        }else{
            imgbtn_map_mypage_f.performClick()
        }

        ////////////////////////////////////////////////////////

    }

    override fun onStart() {
        super.onStart()

        //-----임시 데이터-----------------------------
        Glide.with(this)
            .load(R.mipmap.ic_launcher)
             .into(mToolbar.img_profile_toolbar)
        mToolbar.text_name_toolbar.text="MyNickName"
        //--------------------------------------------
        // 툴바 유저이미지, 유저닉네임, 알람, 메뉴 보이게
        controlToobar(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE)
        mToolbar.visibility = View.VISIBLE

    }

    fun init(){
        mypagePresenter = MypagePresenter(this)

        mToolbar.img_noti_toolbar.setOnClickListener(this)
        floatimgbtn_addphoto_mypage.setOnClickListener(this)

    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_mypageFragment_to_photo)
    }

    override fun showAddPhotoUi(mFilePath : String) {
        val nextIntent = Intent(context, AddPhotoActivity::class.java)
        nextIntent.putExtra("photo", mFilePath)
        startActivity(nextIntent)
        logd("photoTEST", "Mypagefragment에서 넘겨줌 " + mFilePath)
    }

    override fun showAlarmUi() {
        findNavController().navigate(R.id.action_mypageFragment_to_alarmFragment)
    }

    override fun showEditMyInfoUi() {
        startActivity(Intent(context, EditMyInfoActivity::class.java))
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.img_noti_toolbar -> {presenter.openAlarm()}
            R.id.floatimgbtn_addphoto_mypage -> {
                presenter.clickAddPhoto()
            }
        }
    }

    interface mypageInter{
        fun itemClick()
    }

    inner class MypageAdapter(val context: Context, val mypageInterListener:mypageInter):RecyclerView.Adapter<MypageAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MypageAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(holder: MypageAdapter.ViewHolder, position: Int) {

            //------------임시 데이터들---------------------------------------------------------------
            if(position==0 || position==5){
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }else if(position==1 || position==6){
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/06/23/17/41/morocco-2435391_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)

            }else if(position==2 || position==7){
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }else if(position==3 || position==8){
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }else if(position==4 || position==9){
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }
            //---------------------------------------------------------------------------------------------------

            holder.itemView.setOnClickListener {
                mypageInterListener.itemClick()
            }
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

    }

    override fun checkPermission(): Boolean {
        val result = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (result == PackageManager.PERMISSION_DENIED) return false
        return true
    }

    override fun showPermissionDialog() {
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000)
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1000){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                presenter.clickAddPhoto()
            }
        }
    }

    override fun openGallery(){
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(pickPhoto, 102)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         if (resultCode == Activity.RESULT_OK && null != data) {
                if(requestCode == 102) {
                if (data.getData() != null) {
                    var mPhotoPath: Uri = data.getData()
                    logd(TAG, "photopath : " + mPhotoPath)

                    val options = UCrop.Options()
                    options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE)
                    options.setToolbarTitle("")
                    options.setToolbarCropDrawable(R.drawable.ic_arrow_forward_black_24dp)
                    options.setActiveControlsWidgetColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    options.setStatusBarColor(ContextCompat.getColor(context!!, R.color.bg_black))
                    options.setAspectRatioOptions(0,
                        AspectRatio("4X3", 4f, 3f),
                        AspectRatio("1X1", 1f, 1f)
                    )

                    /* 현재시간을 임시 파일 이름에 넣는 이유 : 중복방지
                    / (안넣으면 AddPhotoActivity의 이미지뷰에 다른 사진 보여진다.) */
                    val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
                    UCrop.of(mPhotoPath, Uri.fromFile(File(context!!.cacheDir, timeStamp+SAMPLE_CROPPED_IMAGE_NAME)))
                        .withMaxResultSize(resources.getDimension(R.dimen.upload_width).toInt(),
                            resources.getDimension(R.dimen.upload_heigth).toInt())
                        .withOptions(options)
                        .start(context!!, this)

                        //uCrop 넣기 전(다음 페이지 진행)
//                     presenter.openAddPhoto(mPhotoPath.toString())

                }
            }else if(requestCode == UCrop.REQUEST_CROP){
                    var mCropPath: Uri? = UCrop.getOutput(data)
                    logd(TAG, "croppath : " + mCropPath)
                    presenter.openAddPhoto(mCropPath.toString())
                }
        }
        if(resultCode == UCrop.RESULT_ERROR){

        }
    }

}





