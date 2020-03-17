package com.avon.spott.AddPhoto


import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.*
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.BackgroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.avon.spott.AnimSlide.Companion.animSlideAlpha
import com.avon.spott.FindPlace.FindPlaceActivity
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class AddPhotoActivity : AppCompatActivity(), AddPhotoContract.View, View.OnClickListener,
    OnMapReadyCallback {


    companion object {
        init {
            System.loadLibrary("opencv_java4")
            System.loadLibrary("native-lib")
        }
    }

    private val TAG = "forAddPhotoActivity"

    private lateinit var addPhotoPresenter: AddPhotoPresenter
    override lateinit var presenter: AddPhotoContract.Presenter

    private var markerLatLng: LatLng? = null

    private lateinit var mMap : GoogleMap

    private val hashArrayList = ArrayList<String>() //해시태그리스트

    private var mIsOpenCVReady = false

    private var lowQuality = true

    lateinit var watcher : TextWatcher

    private lateinit var geocoder: Geocoder

    private var moveToPlace = false

    //mylocation
    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    override var  mylocation :LatLng? = null
    private var mylocationClick = false //내 위치 버튼 클릭했는지 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.frag_googlemap_addphoto_a) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //툴바 타이틀 넣기
        include_toolbar_addphoto_a.text_title_toolbar.text = getString(R.string.adding_photo)

        //처음 키보드 올라오기 방지용
        text_guide_addphoto_a.requestFocus()

        init()
    }

    override fun onStart() {
        super.onStart()

        locationInit()

        watcher =
            edit_search_addphoto_a.addTextChangedListener {
                if(it!!.trim().length>0){
                    edit_search_addphoto_a.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_arrow_forward_black_24dp, 0, R.drawable.ic_close_grey_20dp,0)

                }else{
                    edit_search_addphoto_a.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_arrow_forward_black_24dp, 0, 0,0)
                }
            }

    }

    override fun onResume() {
        super.onResume()
        if (OpenCVLoader.initDebug()) {
            mIsOpenCVReady = true
        }
    }

    override fun onStop() {
        super.onStop()

        edit_search_addphoto_a.removeTextChangedListener(watcher)

        //현재 내 위치 받아오기 제거
        mFusedLocationClient.removeLocationUpdates(locationCallback)
        mylocation = null
    }

    private fun locationInit(){
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000) // 자기 위치를 최신화하는 단위시간
            .setFastestInterval(1000)

        val builder : LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    val locationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            locationResult?.let{
                for((i, location) in it.locations.withIndex()){
                    logd(TAG, "mylocation #$i ${location.latitude} , ${location.longitude}")
                    mylocation = LatLng(location.latitude, location.longitude)
                    if(mylocationClick && mylocation!=null){
                        mylocationClick = false
                        showMylocation()
                        animCamera(mylocation!!)
                        showProgressbar(false)
                    }

                }
            }

        }
    }

    override fun showMylocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }

    override fun showProgressbar(boolean: Boolean) {
        progress_addphoto_a.visibility = if(boolean) View.VISIBLE else View.GONE
    }

    override fun startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    external fun detectEdgeJNI(inputImage: Long, outputImage: Long, th1: Int, th2: Int, th3:Int)

    override fun detectEdgeUsingJNI(mInputImage:Bitmap) : Bitmap {

        mInputImage!!.setHasAlpha(true)
        val src = Mat()
        Utils.bitmapToMat(mInputImage, src)
        val edge = Mat()
        detectEdgeJNI(src.nativeObjAddr, edge.nativeObjAddr, 5, 15, 17)
        Utils.matToBitmap(edge, mInputImage)

        return mInputImage

    }

    private fun init(){
        addPhotoPresenter = AddPhotoPresenter(this)
        text_upload_addphoto_a.setOnClickListener(this)
        include_toolbar_addphoto_a.img_back_toolbar.setOnClickListener(this)
//        imgbtn_search_addphoto_a.setOnClickListener(this)
        imgbtn_mylocation_addphoto_a.setOnClickListener(this)

        checkQuality(intent.getStringExtra("cropPhoto"))
        
        edit_search_addphoto_a.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if(edit_search_addphoto_a.text.toString().trim().length>0){
                    findPlace(edit_search_addphoto_a.text.toString())
                    edit_search_addphoto_a.hideKeyboard()
                }else{
                    showToast(getString(R.string.toast_blank_edit_search_addphoto_a))
                }

               true
            }else false
        }


        edit_search_addphoto_a.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
              when(event?.action){
                    MotionEvent.ACTION_UP ->{
                            logd(TAG, "event.rawX :"+ event.rawX)
                            logd(TAG, "left : "+ edit_search_addphoto_a.left)
                            logd(TAG, "width : "+edit_search_addphoto_a.compoundDrawables[0].bounds.width())
                            if(event.rawX <= (edit_search_addphoto_a.left + edit_search_addphoto_a.compoundDrawables[0].bounds.width()+20)){
                                // 검색 EditText 없애기
                                edit_search_addphoto_a.text.clear()

                                edit_search_addphoto_a.clearFocus()
                                edit_caption_addphoto_a.clearFocus()
                                edit_search_addphoto_a.hideKeyboard()
                                edit_caption_addphoto_a.hideKeyboard()


                                animSlideAlpha(this@AddPhotoActivity, edit_search_addphoto_a, false)
                                Handler().postDelayed({
                                    imgbtn_search_addphoto_a.visibility = View.VISIBLE
                                }, 250)

                                return v?.onTouchEvent(event) ?: true
                            }else if(edit_search_addphoto_a.compoundDrawables[2]!=null){
                               if(event.rawX >= (edit_search_addphoto_a.right - edit_search_addphoto_a.compoundDrawables[2].bounds.width()-30)){
                                   // EditText 내용 지우기
                                   edit_search_addphoto_a.text.clear()
                                   return v?.onTouchEvent(event) ?: true
                               }
                            }


                    }
                }

                return v?.onTouchEvent(event) ?: false
            }
        })


        edit_caption_addphoto_a.addTextChangedListener {
            hashArrayList.clear()
            presenter.checkEdit(it)
        }

        imgbtn_search_addphoto_a.setOnClickListener {
            edit_caption_addphoto_a.hideKeyboard()
            edit_search_addphoto_a.clearFocus()
            edit_caption_addphoto_a.clearFocus()
            animSlideAlpha(this, imgbtn_search_addphoto_a, true)
            Handler().postDelayed({
                edit_search_addphoto_a.visibility = View.VISIBLE
                edit_search_addphoto_a.requestFocus()
            }, 250)


        }



        edit_search_addphoto_a.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }
        }

        edit_caption_addphoto_a.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus){
                logd(TAG, "edit_caption_addphoto_a + hasfocus")
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }



    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.text_upload_addphoto_a->{
                if (!mIsOpenCVReady) {
                    return
                }
                presenter.sendPhoto(getString(R.string.baseurl),intent.getStringExtra("cropPhoto"),
                    edit_caption_addphoto_a.text.toString(), markerLatLng, hashArrayList, lowQuality)
            }
            R.id.img_back_toolbar ->{ presenter.navigateUp() }
//            R.id.imgbtn_search_addphoto_a ->{presenter.openFindPlace()}
            R.id.imgbtn_mylocation_addphoto_a ->{
                mylocationClick = true
                presenter.getMylocation()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        geocoder = Geocoder(this)

        mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기(회전 불가)

        //인텐트로 받아온 사진 이미지 처리(이미지뷰에 넣기 + 위치 정보 있으면 가져오기)
        presenter.usePhoto(intent.getStringExtra("cropPhoto"))

        //맵 롱클릭 리스너
        mMap.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener{
            override fun onMapLongClick(latlng: LatLng) {
                logd(TAG, "MapLONGClick : "+latlng)
                presenter.newMarker(latlng)
            }
        })

        mMap.setOnCameraMoveStartedListener {
            if(!moveToPlace){
                if(edit_search_addphoto_a.visibility == View.VISIBLE){
                    edit_search_addphoto_a.clearFocus()
                    edit_caption_addphoto_a.clearFocus()
                    edit_search_addphoto_a.hideKeyboard()
                    edit_caption_addphoto_a.hideKeyboard()

                    animSlideAlpha(this, edit_search_addphoto_a, false)
                    Handler().postDelayed({
                        imgbtn_search_addphoto_a.visibility = View.VISIBLE
                    }, 250)
                }

            }
            moveToPlace = false

        }



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

    override fun showFindPlaceUi() {
        startActivity(Intent(this, FindPlaceActivity::class.java))
    }

    override fun makeFile(bitmap:Bitmap, name:String, quality:Int, type:Int) : File{
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val file = File(applicationContext.cacheDir, timeStamp+name)
        file.createNewFile()
        val out = FileOutputStream(file)

        bitmap!!.compress(if(type ==0) Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG
            , quality , out)

        out.close()

        return file
    }

    private fun checkQuality(photoPath:String){
        val path =getPath(Uri.parse(photoPath))
        val mOriginalImage = BitmapFactory.decodeFile(path, BitmapFactory.Options())
        val file = File(path)
        val size = (file.length()/1024).toString() //사이즈 크기 kB

        if(mOriginalImage!!.width<200 || mOriginalImage!!.height<200 || size.toInt()<500){
            Toast.makeText(this, R.string.toast_low_quality, Toast.LENGTH_LONG).show()
        }else lowQuality = false

        logd(TAG, "size : $size")
        logd(TAG, "width : " + mOriginalImage!!.width)
        logd(TAG, "height : " + mOriginalImage!!.height)
    }

    override fun showErrorToast() {
        showToast(getString(R.string.server_connection_error))
    }

    override fun showNoLoactionInfoToast() {
        showToast(getString(R.string.no_location_info))
    }

    override fun showNoCaptionToast() {
        showToast(getString(R.string.no_photo_caption))
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    private fun findPlace(str:String){
        var addressList : List<Address>? = null

        try {
            addressList = geocoder.getFromLocationName(
                str, 10
            )
        }catch (e:IOException){
            e.printStackTrace()
        }

        if(addressList!=null && addressList!!.size > 0){
            logd(TAG, "address : " + addressList!![0].toString())
            moveToPlace = true

            val center: CameraUpdate = CameraUpdateFactory.newLatLng(LatLng(addressList!![0].latitude, addressList!![0].longitude))
            mMap.animateCamera(center, 400, null)
            addMarker(LatLng(addressList!![0].latitude, addressList!![0].longitude))

        }else{
            showToast(getString(R.string.text_no_place))
        }
    }

    override fun animCamera(latLng: LatLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 400, null)
        addMarker(latLng)
    }

    override fun checkPermission(): Boolean {
        val resultFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if( resultFine == PackageManager.PERMISSION_DENIED ||
            resultCoarse == PackageManager.PERMISSION_DENIED ) return false
        return true
    }

    override fun showPermissionDialog() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==1000){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                presenter.getMylocation()
            }
        }
    }

}
