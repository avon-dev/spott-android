package com.avon.spott.AddPhoto


import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.BackgroundColorSpan
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.util.regex.Pattern

class AddPhotoActivity : AppCompatActivity(), AddPhotoContract.View, View.OnClickListener,
    OnMapReadyCallback {
    private val TAG = "forAddPhotoActivity"

    private lateinit var addPhotoPresenter: AddPhotoPresenter
    override lateinit var presenter: AddPhotoContract.Presenter

    private var markerLatLng: LatLng? = null

    private lateinit var mMap : GoogleMap

    private val hashArrayList = ArrayList<String>() //해시태그리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_googlemap_addphoto_a) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //툴바 타이틀 넣기
        include_toolbar_addphoto_a.text_title_toolbar.text = getString(R.string.adding_photo)

        //처음 키보드 올라오기 방지용
        text_guide_addphoto_a.requestFocus()

//        switch_private_addphoto_a.setOnCheckedChangeListener { buttonView, isChecked ->
//            if(isChecked){
//                text_public_addphoto_a.visibility = View.VISIBLE
//                text_private_addphoto_a.visibility = View.GONE
//            }else{
//                text_public_addphoto_a.visibility = View.GONE
//                text_private_addphoto_a.visibility = View.VISIBLE
//            }
//        }
//
//        switch_private_addphoto_a.isChecked = true

        init()
    }

    private fun init(){
        addPhotoPresenter = AddPhotoPresenter(this)
        text_upload_addphoto_a.setOnClickListener(this)
        include_toolbar_addphoto_a.img_back_toolbar.setOnClickListener(this)


        edit_caption_addphoto_a.addTextChangedListener {

            hashArrayList.clear()
            presenter.checkEdit(it)


        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.text_upload_addphoto_a->{
                presenter.sendPhoto(getString(R.string.baseurl),intent.getStringExtra("photo"),
                    edit_caption_addphoto_a.text.toString(), markerLatLng, hashArrayList)
            }
            R.id.img_back_toolbar ->{ presenter.navigateUp() }
        }
    }

    override fun onMapReady(map: GoogleMap) {
             mMap = map

        //인텐트로 받아온 사진 이미지 처리(이미지뷰에 넣기 + 위치 정보 있으면 가져오기)
        presenter.usePhoto(intent.getStringExtra("photo"))

        //맵 롱클릭 리스너
        mMap.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener{
            override fun onMapLongClick(latlng: LatLng) {
                logd(TAG, "MapLONGClick : "+latlng)
                presenter.newMarker(latlng)
            }
        })

    }

    override fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    override fun getPath(uri: Uri): String {

        val path:String
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        logd(TAG, "cursor : "+cursor)
        if(cursor == null){
            path = uri.path
        }else{
            cursor.moveToFirst()
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            path = cursor.getString(column_index)
        }
        return path
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun setPhoto(photo:String){
        logd("photoTEST", "이미지 넣기 직전 : " + photo)
        Glide.with(this)
            .load(photo)
            .apply(RequestOptions().centerCrop())
            .into(img_preview_addphoto_a)
    }

    override fun movePosition(latLng: LatLng, zoom: Float) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun addMarker(latLng: LatLng){
        mMap.clear()
        val makerOptions = MarkerOptions()
        makerOptions.position(latLng)
              .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_where_to_vote_black_48))
        mMap.addMarker(makerOptions)

        //현재 마커의 위치 저장
        markerLatLng = latLng
    }

    override fun focusEdit(){ //설명 editText 포커스
        edit_caption_addphoto_a.requestFocus()
    }

    override fun showLoading(boolean: Boolean){
        linear_loading_addphoto_a.visibility = if(boolean) View.VISIBLE else View.GONE
    }

    override fun enableTouching(boolean: Boolean){
        if(!boolean){
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }else{
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    override fun addHashtag(hashtag:String){
        hashArrayList.add(hashtag)
        logd("hashtest","hashtest : "+hashArrayList)
    }

    override fun highlightHashtag(boolean:Boolean,editable: Editable?, start:Int, end:Int){ //해시태그 하이라이트 켜고 끄는 함수

        if(boolean){
            editable!!.setSpan(
                BackgroundColorSpan(ContextCompat.getColor(this, R.color.hashtag_highlight)),
                start,
                end,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )


        }else{ //하이라이트 제거
            val spans = editable!!.getSpans(0,editable.toString().length, BackgroundColorSpan::class.java)
            for(span in spans){
                editable!!.removeSpan(span)
            }

        }


    }

    override fun getCursorPostion():Int{ //EditText의 현재 커서 위치
        return edit_caption_addphoto_a.selectionEnd
    }

}
