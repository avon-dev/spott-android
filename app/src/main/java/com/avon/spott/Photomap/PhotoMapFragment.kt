package com.avon.spott.Photomap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.Utils.logd
import com.avon.spott.getMarkerBitmapFromView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_photomap.*
import kotlinx.android.synthetic.main.toolbar.view.*

class PhotoMapFragment: Fragment(), PhotoMapContract.View, View.OnClickListener,
    OnMapReadyCallback {

    private val TAG = "forPhotoMapFragment"

    private lateinit var photoMapPresenter: PhotoMapPresenter
    override lateinit var presenter: PhotoMapContract.Presenter

    //googlemap
    private lateinit var mMap : GoogleMap
    private lateinit var mapView : View

    //이 사진의 위도,경도
    private lateinit var photoLatLng : LatLng

    //mylocation
    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    override var  mylocation :LatLng? = null
    private var mylocationClick = false //내 위치 버튼 클릭했는지 여부

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photomap, container, false)

        if(!::mapView.isInitialized){ //처음 생성될 때 빼고는 다시 구글맵을 초기화하지 않는다.
            val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.frag_googlemap_photomap_f) as SupportMapFragment
            mapFragment.getMapAsync(this)
            mapView = mapFragment.view!!
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()

        locationInit()

        //툴바 뒤로가기, 타이틀 보이게
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE,View.GONE, View.GONE, View.GONE, View.GONE, View.GONE)
        mToolbar.text_title_toolbar.text = getString(R.string.map)
        mToolbar.visibility = View.VISIBLE
    }

    override fun onStop() {
        super.onStop()

        //현재 내 위치 받아오기 제거
        mFusedLocationClient.removeLocationUpdates(locationCallback)
        mylocation = null
    }

    fun init(){
        photoMapPresenter = PhotoMapPresenter(this)
        imgbtn_mylocation_photomap_f.setOnClickListener(this)
        imgbtn_spot_photomap_f.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgbtn_mylocation_photomap_f -> {
                mylocationClick = true
                presenter.getMylocation()
            }
            R.id.imgbtn_spot_photomap_f->{
                presenter.moveToSpot(photoLatLng)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        //넘어오는 사진의 위도 경도
        val photoLat = arguments?.getFloat("photoLat")
        val photoLng = arguments?.getFloat("photoLng")

        photoLatLng = LatLng(photoLat!!.toDouble(), photoLng!!.toDouble())


        val photoUrl = arguments?.getString("photoUrl")

        val customMarkerView = LayoutInflater.from(context!!).inflate(R.layout.marker_photo, null)

        Glide.with(context!!)
            .asBitmap()
            .load(photoUrl)
            .fitCenter()
            .into(object : CustomTarget<Bitmap>(){
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    try{
                        val makerOptions = MarkerOptions()
                        makerOptions.position(photoLatLng).icon(BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(customMarkerView, bitmap, 1,false, context!!, 200)))

                        mMap.addMarker(makerOptions)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(photoLatLng, 16f))

                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    logd(TAG, "onLoadFailed" + errorDrawable)
                }
            })
    }

    override fun animCamera(latLng: LatLng){
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun checkPermission(): Boolean {
        val resultFine = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCoarse = ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION)
        if( resultFine == PackageManager.PERMISSION_DENIED ||
            resultCoarse == PackageManager.PERMISSION_DENIED ) return false
        return true
    }

    override fun showPermissionDialog() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1000)
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

    override fun showMylocation() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = false
    }

    override fun showProgressbar(boolean: Boolean) {
        progress_photomap_f.visibility = if(boolean) View.VISIBLE else View.GONE
    }

    private fun locationInit(){
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000) // 자기 위치를 최신화하는 단위시간
            .setFastestInterval(1000)

        val builder : LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
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

    override fun startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }
}