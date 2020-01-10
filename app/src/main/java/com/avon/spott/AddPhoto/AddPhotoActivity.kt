package com.avon.spott.AddPhoto


import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
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
import java.io.IOException
import java.util.*

class AddPhotoActivity : AppCompatActivity(), AddPhotoContract.View, View.OnClickListener,
    OnMapReadyCallback {

    private val TAG = "forAddPhotoActivity"

    private lateinit var addPhotoPresenter: AddPhotoPresenter
    override lateinit var presenter: AddPhotoContract.Presenter

    private lateinit var mMap : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_googlemap_addphoto_a) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //툴바 타이틀 넣기
        include_toolbar_addphoto_a.text_title_toolbar.text = getString(R.string.adding_photo)

        init()
    }

    private fun init(){
        addPhotoPresenter = AddPhotoPresenter(this)
        btn_upload_addphoto_a.setOnClickListener(this)
        include_toolbar_addphoto_a.img_back_toolbar.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_upload_addphoto_a->{
                presenter.sendPhoto(getString(R.string.testurl),intent.getStringExtra("photo"), edit_caption_addphoto_a.text.toString(),
                    position())
            }
            R.id.img_back_toolbar ->{ presenter.navigateUp() }
        }
    }

    override fun onMapReady(map: GoogleMap) {
             mMap = map

        //인텐트로 받아온 사진 이미지 처리(이미지뷰에 넣기 + 위치 정보 있으면 가져오기)
        presenter.usePhoto(intent.getStringExtra("photo"))
    }

    /*==========================구글맵 더미 데이터용 랜덤위치 생성 함수들==================================*/
    private fun position(): LatLng {
        return LatLng(random(37.489324, 37.626495), random(126.903712, 127.096659))
    }

    private fun random(min:Double, max:Double):Double{
        return min+(max-min)* Random().nextDouble()
    }
    /*=========================구글맵 더미 데이터용 랜덤위치 생성 함수들 끝=================================*/

    override fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    override fun getPath(uri: Uri): String {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = managedQuery(uri, projection, null, null, null)
//        startManagingCursor(cursor)
//        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//        cursor.moveToFirst()
//        return cursor.getString(column_index)

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun setPhoto(photo:String){
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
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        mMap.addMarker(makerOptions)
    }
}
