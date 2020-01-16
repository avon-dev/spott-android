package com.avon.spott.Map


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.MapCluster
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map_list.*
import kotlin.collections.ArrayList

class MapFragment : Fragment() , MapContract.View, View.OnClickListener, OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MapCluster>, ClusterManager.OnClusterItemClickListener<MapCluster>{

    private val TAG = "forMapFragment"

    //presenter
    private lateinit var mapPresenter : MapPresenter
    override lateinit var presenter : MapContract.Presenter

    //recyclerview
    private lateinit var mapAdapter: MapAdapter

    //맵리스트플래그먼트(하단플래그먼트)와 바텀시트 움직임 관리
    lateinit var childfragment : Fragment
    lateinit var mBottomSheetBehavior : BottomSheetBehavior<View?>

    //googlemap
    private lateinit var mMap : GoogleMap
    private lateinit var mapView : View

    //googlemap clustering
    private lateinit var clusterManager : ClusterManager<MapCluster>
    private lateinit var  mCustomClusterItemRenderer : PhotoRenderer

    //전에 선택된 마커 처리
    private var selectedMarker : Marker? = null //선택한 마커
    private var selectedMarkerView : View? = null //선택했던 마커뷰
    private var selectedCluster : Cluster<MapCluster>? = null //선택한 클러스터
    private var selectedItems : ArrayList<MapCluster>? = null//선택된 아이템

    //카메라 움직임이 클러스터 선택으로 인한 움직임인지 확인, 클러스터 선택으로 인한 움직임이면 데이터 새로 가져오지 않음
    private var clusterSelectMove : Boolean = false

    //mylocation
    private lateinit var mFusedLocationClient : FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest
    override var  mylocation :LatLng? = null
    private var mylocationClick = false //내 위치 버튼 클릭했는지 여부

    //paging 테스트중.. +++++++++++++++
//    private var start = 0
//    private val pageItems = 6  //아이템수
//    private var pageLoading = false
    //+++++++++++++++++++++++++++++++++

    // 어댑터와 뷰 연결
    val mapInterListener = object : mapInter{
        override fun itemClick(id: Int){ //  맵리스트플래그먼트(하단플래그먼트) 리사이클러뷰 아이템 클릭시
            presenter.openPhoto(id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        configureBackdrop()  //바텀시트 처리

        if(!::mapView.isInitialized){ //처음 생성될 때 빼고는 다시 구글맵을 초기화하지 않는다.
            val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.frag_googlemap_map_f) as SupportMapFragment
            mapFragment.getMapAsync(this)
            mapView = mapFragment.view!!
        }


        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //맵리스트플래그먼트(하단플래그먼트)의 리사이클러뷰 생성
        val layoutManager = GridLayoutManager(context!!, 2)
        recycler_map_f.layoutManager = layoutManager
        mapAdapter = MapAdapter(context!!, mapInterListener)
        recycler_map_f.adapter = mapAdapter

        //+++++++++++++++++++++++++++++++++++++++++++
//        recycler_map_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if(!recycler_map_f.canScrollVertically(1)){
//                    if(selectedItems!=null && !pageLoading){
//                        pageLoading = true
//                        Handler().postDelayed({
//                        addSomeItems()
//                        }, 1000)
//                    }
//                }
//            }
//        })

        //+++++++++++++++++++++++++++++++++++++++

    }

    fun init(){
        mapPresenter = MapPresenter(this)
        imgbtn_mylocation_map_f.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        locationInit()

        //툴바 안보이게
        mToolbar.visibility = View.GONE

        //바텀시트가 현재 어떤 상태인지 확인하고 해당 상태에 맞게 ui 처리
        if(mBottomSheetBehavior?.state == STATE_EXPANDED){
            bottomExpanded()
        }else{
            bottomCollapsed()
        }

        //선택했던 아이템 있을 경우 맵리스트플래그먼트(하단플래그먼트)의 리사이클러뷰와 전체 개수 텍스트 처리
        if(selectedItems!=null){

            mapAdapter.addItemsAdapter(selectedItems!!)
            mapAdapter.notifyDataSetChanged()

            text_spotnumber_maplist_f.text = selectedItems!!.size.toString()
        }
    }

    private fun configureBackdrop() { //바텀시트 처리

        childfragment = childFragmentManager?.findFragmentById(R.id.frag_list_map_f)!!  // 하단 내비게이션으로 쓸 플래그먼트 선택

        childfragment?.let {
            // Get the BottomSheetBehavior from the fragment view
            // 플래그먼트
            BottomSheetBehavior.from(it.view)?.let { bsb ->

                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                childfragment.img_updown_maplist_f.setOnClickListener {
                    if( bsb.state == BottomSheetBehavior.STATE_COLLAPSED || bsb.state == BottomSheetBehavior.STATE_HALF_EXPANDED){
                        bsb.state = BottomSheetBehavior.STATE_EXPANDED
                    }else{

                        bsb.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                // Set the reference into class attribute (will be used latter)
                mBottomSheetBehavior = bsb

                bsb.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        // React to state change
                        when (newState) {
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                bottomExpanded()
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                bottomCollapsed()
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }
                })
            }

        }
    }

    fun bottomExpanded(){ //맵리스트플래그먼트(하단플래그먼트)가 올라와있을 때 일어나는 일
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        imgbtn_mylocation_map_f.isEnabled =false
        if(::mMap.isInitialized) {
            mMap.uiSettings.setAllGesturesEnabled(false)
        }
    }

    fun bottomCollapsed(){ //맵리스트플래그먼트(하단플래그먼트)가 내려가있을 때 일어나는 일
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
        imgbtn_mylocation_map_f.isEnabled=true
        if(::mMap.isInitialized){
            mMap.uiSettings.setAllGesturesEnabled(true)
            mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기
        }
    }

    override fun onClick(v: View?){
        when(v?.id){
            R.id.imgbtn_mylocation_map_f ->{ //내 위치 이미지버튼 ->
                mylocationClick = true
                presenter.getMylocation()
            }
        }
    }

    override fun showPhotoUi(id:Int){ //PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_mapFragment_to_photo, bundle)
    }

    interface mapInter{
        fun itemClick(id: Int)
    }

    inner class MapAdapter(val context: Context, val  mapInterListener:mapInter):RecyclerView.Adapter<MapAdapter.ViewHolder>(){

        private var itemsList = ArrayList<MapCluster>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(mapItems: ArrayList<MapCluster>){
            itemsList = mapItems
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        //paging 테스트중.. +++++++++++++++
        fun addPageItem(mapItem : MapCluster){
            itemsList.add(mapItem)
        }
        //++++++++++++++++++++++++++++++++++++

        override fun onBindViewHolder(holder: MapAdapter.ViewHolder, position: Int) {

            itemsList[position].let{
                     Glide.with(holder.itemView.context)
                    .load(it.posts_image)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }


            holder.itemView.setOnClickListener{
                mapInterListener.itemClick(itemsList[position].id)
            }
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

    }



    override fun onMapReady(map: GoogleMap){ //구글맵이 로딩 되었을 때
        mMap = map


        mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기(회전 불가)

        setClusterManager()

        presenter.getLastPosition() //카메라 포지션 옮기기. (처음실행 : 서울, 그외 : 최근에 봤던 곳)

    }

    override fun movePosition(latLng: LatLng, zoom:Float){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        logd(TAG, "movePosition")
    }


    /* 사진이미지가 들어있는 풍선 모양 마커 비트맵을 만드는 함수 */
    private fun getMarkerBitmapFromView(view:View, bitmap: Bitmap, size:Int, selected:Boolean): Bitmap{
        val markerImageView = view.findViewById(R.id.img_photo_photo_m) as ImageView
        val countTextView = view.findViewById(R.id.text_count_photo_m) as TextView
        val markerCardView:CardView = view.findViewById(R.id.card_photo_m) as CardView
        val markerTriangleView = view.findViewById(R.id.view_triangle_photo_m) as View

        //클러스터면(사진 여러장이면) 카운트 텍스트 보이게, 클러스터아이템이면 사라지게
        if(size<2){
            countTextView.visibility = View.INVISIBLE
        }else{
            countTextView.visibility = View.VISIBLE
        }
        countTextView.text = size.toString()


        if(selected){ //선택된 클러스터 색깔 바꿈.
            markerCardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.markerSelected))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_select_24dp)
        }else{ //전에 선택되었던 클러스터 색깔 다시 하얀색으로 바꿈.
            markerCardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.text_white))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_white_24dp)
        }

        markerImageView.setImageBitmap(bitmap)

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight() )

        val returnedBitmap = Bitmap.createBitmap(
            view.getMeasuredWidth(), view.getMeasuredHeight(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN)
        val drawable =view.getBackground()
        if (drawable != null)
            drawable!!.draw(canvas)
        view.draw(canvas)
        return returnedBitmap
    }

    /*-------------------------------[클러스터 랜더링 클래스 생성]--------------------------------------------*/
    private inner class PhotoRenderer:DefaultClusterRenderer<MapCluster>(context!!, mMap, clusterManager){

        val customMarkerView:View
        private val mClusterIconGenerator = IconGenerator(context)

        private val icon: Bitmap

        init{
            customMarkerView = LayoutInflater.from(context).inflate(R.layout.marker_photo, null)
            mClusterIconGenerator.setContentView(customMarkerView)

            //임시로 불러와지는 비트맵을 만든다. -> empty bitmap (임시 비트맵을 안만들면 구글 기본 빨간 마커가 보인다.)
            var conf: Bitmap.Config = Bitmap.Config.ARGB_8888 // see other conf types
            var bmp = Bitmap.createBitmap(50, 50, conf) // this creates a MUTABLE bitmap

            icon = bmp
        }

        override fun onBeforeClusterItemRendered(item: MapCluster?, markerOptions: MarkerOptions?) {
            //클러스터아이템 만들어지기 전 임시로 나오는 아이콘 지정 -> empty bitmap
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun onClusterItemRendered(clusterItem: MapCluster?, marker: Marker?) {
            selectedMarker = null //선택된 클러스터 null로 만듦. -> 새로 클러스터링이 되면 선택했던 클러스터 없어지게함.
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED //리스트플래그먼트는 내려가게함.

            Glide.with(context!!)
                .asBitmap()
                .load(clusterItem!!.posts_image)
                .fitCenter()
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(customMarkerView, bitmap, 1,false)))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        logd(TAG, "onLoadFailed" + errorDrawable)
                    }
                })
        }


        override fun onBeforeClusterRendered(cluster: Cluster<MapCluster>?, markerOptions: MarkerOptions?) {
            //클러스터 만들어지기 전 임시로 나오는 아이콘 지정 -> empty bitmap
            markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun onClusterRendered(cluster: Cluster<MapCluster>?, marker: Marker?) {
            selectedMarker = null//선택된 클러스터 null로 만듦. -> 새로 클러스터링이 되면 선택했던 클러스터 없어지게함.
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED //맵리스트플래그먼트(하단플래그먼트)는 내려가게함.

            val firstItem = cluster!!.items.iterator().next()   //첫번째 아이템 선택

            Glide.with(context!!)
                .asBitmap()
                .load(firstItem.posts_image)
                .fitCenter()
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?){
                        marker!!.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(customMarkerView,bitmap, cluster.size, false)))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                            logd(TAG, "onLoadFailed" + errorDrawable)
                    }

                })
        }

        override fun shouldRenderAsCluster(cluster: Cluster<MapCluster>?): Boolean {
            // 언제나 클러스터가 일어나게함. 클러스터아이템이 2개 이상이면 클러스터가 일어나게함.
            return cluster!!.size > 1
        }
    }
    /*-------------------------------[클러스터 랜더링 클래스 끝]--------------------------------------------*/


    override fun onClusterItemClick(item: MapCluster?): Boolean { //클러스터아이템(사진 한 장) 눌렀을 때 일어나는 일
        if(mBottomSheetBehavior?.state ==  BottomSheetBehavior.STATE_EXPANDED){
            return true   //맵리스트플래그먼트(하단플래그먼트)가 올라온 상태라면 클러스터 클릭안되게
        }
        presenter.openPhoto(item!!.id) //사진 한 장 클릭시 PhotoFragment로 이동
        return true
    }

    override fun onClusterClick(cluster: Cluster<MapCluster>?): Boolean { //클러스터(사진 여러장) 눌렀을 때 일어나는 일

        if(mBottomSheetBehavior?.state ==  BottomSheetBehavior.STATE_EXPANDED){
            //맵리스트플래그먼트(하단플래그먼트)가 올라온 상태라면 클러스터 클릭안되게
            return true
        }


        if(selectedMarker !=null){ //전에 선택했던 마커는 다시 하얀색으로 바꾸기
            Glide.with(context!!)
                .asBitmap()
                .load(selectedCluster!!.items.iterator().next().posts_image) //전에 선택했던 클러스터의 첫번째 이미지 url가져온다.
                .fitCenter()
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        selectedMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                            // 전에 선택했던 클러스터 아이템의 이미지를 넣은 클러스터 마커의 비트맵(테두리 하얀색 마커)을 만든다.
                            getMarkerBitmapFromView(selectedMarkerView!!, bitmap, selectedCluster!!.size,false)))
                        newCluster(cluster) // 새로 선택된 클러스터의 이미지를 넣은 클러스터 마커의 비트맵(테두리 파란색 마커)을 만든다.
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }

        if(selectedMarker == null) { // 전에 선택된 클러스터가 없다면
            newCluster(cluster) // 바로 새로 선택된 클러스터의 이미지를 넣은 클러스터 마커의 비트맵(테두리 파란색 마커)을 만든다.
        }

        return true
    }

    private fun newCluster(cluster: Cluster<MapCluster>?){ //새로 선택한 클러스터 처리
        childfragment.text_spotnumber_maplist_f.text = cluster!!.size.toString()

        val firstItem = cluster!!.items.iterator().next() //첫번째 아이템 선택

        selectedCluster = cluster
        selectedMarker = mCustomClusterItemRenderer.getMarker(cluster)
        selectedMarkerView = mCustomClusterItemRenderer.customMarkerView

        Glide.with(context!!)
            .asBitmap()
            .load(firstItem.posts_image)
            .fitCenter()
            .into(object :CustomTarget<Bitmap>(){
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?){
                    selectedMarker!!.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(selectedMarkerView!!, bitmap, cluster.size, true)))
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    logd(TAG, "onLoadFailed" + errorDrawable)
                }
            })
        mapAdapter.clearItemsAdapter()

        val clusterMapItems = ArrayList<MapCluster>()
        clusterMapItems.addAll(cluster!!.items)

        selectedItems = clusterMapItems //선택된 아이템 변경.

        mapAdapter.addItemsAdapter(clusterMapItems)
        mapAdapter.notifyDataSetChanged()

        //클러스터 선택시 맵리스트플래그먼트(하단플래그먼트) 반만 올라오게
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED


        val quarterLat = //지금 보여지는 맵 위쪽과 아래쪽의 위도 차이를 구해서 나눔.
            (mMap.projection.visibleRegion.latLngBounds.northeast.latitude - mMap.projection.visibleRegion.latLngBounds.southwest.latitude)/6

         val newClusterLatLng = //클러스터의 위도에서 바로 위에 구한 위도차이를 빼줌
            LatLng(cluster.position.latitude - quarterLat, cluster.position.longitude)

        val center: CameraUpdate = CameraUpdateFactory.newLatLng(newClusterLatLng)
        mMap.animateCamera(center, 400, object : GoogleMap.CancelableCallback{
            override fun onFinish() {
                clusterSelectMove = true
                logd(TAG, "animation Finish, animMove : "+ clusterSelectMove)
            }

            override fun onCancel() {
            }

        }) //카메라 이동
    }


    override fun onStop() {
        super.onStop()
        presenter.setLastPosition(mMap.cameraPosition) //마지막 구글맵 위치(위도,경도,줌) shared에 저장

        mFusedLocationClient.removeLocationUpdates(locationCallback)
        mylocation = null
    }

    override fun addItems(mapItems:ArrayList<MapCluster>){

        selectedItems = mapItems //선택된 아이템 변경.

        clusterManager.clearItems() //클러스터 삭제
        clusterManager.addItems(selectedItems)
        clusterManager.cluster()

        childfragment.text_spotnumber_maplist_f.text = clusterManager.algorithm.items.size.toString()

        //맵리스트플래그먼트(하단플래그먼트) 리사이클러뷰에 아이템 추가
//        mapAdapter.addItemsAdapter(selectedItems!!)
//        mapAdapter.notifyDataSetChanged()

        //++++++++++페이징 테스트++++++
//           addSomeItems()
        //+++++++++++++++

        //맵리스트플래그먼트(하단플래그먼트) 내려가게
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomCollapsed()
    }


    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun sendCameraRange(){ //새로운 데이터를 가져오기 위해 현재 화면에 보여지는 위치를 서버에 보냄.
       presenter.getPhotos(getString(R.string.baseurl), mMap.projection.visibleRegion.latLngBounds)
    }

    private fun setClusterManager(){
        clusterManager = ClusterManager<MapCluster>(context, mMap)

        mCustomClusterItemRenderer = PhotoRenderer()
        clusterManager!!.renderer = mCustomClusterItemRenderer
        clusterManager!!.renderer.setAnimation(false)

        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)
        clusterManager!!.setOnClusterClickListener(this)
        clusterManager!!.setOnClusterItemClickListener(this)


        mMap.setOnCameraIdleListener(object:GoogleMap.OnCameraIdleListener{ //카메라 움직임이 끝나는 콜백
            override fun onCameraIdle() {
                logd(TAG, "onCameraIdle(), animMove : "+ clusterSelectMove)

                if(!clusterSelectMove){ //애니메이션으로 움직인게 아니라면 데이터 가져옴.
                    text_nophoto_maplist_f.visibility = View.GONE
                    sendCameraRange()
                }
                clusterSelectMove =false

            }
        } )

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

    override fun moveToMylocation() {
        if(mylocation!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 16f))
        }else{
            showToast("현재 위치를 가지고 오는 중입니다. 잠시 후 다시 시도해주세요.")
        }
    }

    override fun progress(boolean: Boolean){
        progress_map_f.visibility = if(boolean) View.VISIBLE else View.GONE
    }

    private fun locationInit(){
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
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
                    Log.d(TAG, "mylocation #$i ${location.latitude} , ${location.longitude}")
                    mylocation = LatLng(location.latitude, location.longitude)
                    if(mylocationClick && mylocation!=null){
                        mylocationClick = false
                        showMylocation()
                        moveToMylocation()
                        progress(false)
                    }

                }
            }

        }
    }

    override fun startLocationUpdates(){
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    override fun noPhoto(){ //카메라 범위에 해당하는 사진이 없을때
        text_spotnumber_maplist_f.text = "0"
        text_nophoto_maplist_f.visibility = View.VISIBLE
    }

//    +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//    private fun addSomeItems(){
//
//        var end = start + pageItems
//        if(end>selectedItems!!.size){
//            end = selectedItems!!.size
//        }
//
//        if(start < selectedItems!!.size){
//            for(i in start..end){
//                mapAdapter.addPageItem(selectedItems!![i])
//            }
//            start = start + pageItems
//        }
//       mapAdapter.notifyDataSetChanged()
//       pageLoading = false
//    }
//   +++++++++++++++++++++++++++++++++++++++++++++++++++++++++

}





