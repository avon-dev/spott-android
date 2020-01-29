package com.avon.spott.Map


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.AddPhoto.AddPhotoActivity
import com.avon.spott.Data.MapCluster
import com.avon.spott.EmailLogin.EmailLoginActivity
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.MainActivity.Companion.toFirstMapFragment
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
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_list.*
import kotlin.collections.ArrayList

class MapFragment : Fragment() , MapContract.View, View.OnClickListener, OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MapCluster>, ClusterManager.OnClusterItemClickListener<MapCluster>{

    private val TAG = "forMapFragment"

    companion object{
         lateinit var mBottomSheetBehavior : BottomSheetBehavior<ConstraintLayout?>
    }

    //presenter
    private lateinit var mapPresenter : MapPresenter
    override lateinit var presenter : MapContract.Presenter

    //recyclerview
    private lateinit var mapAdapter: MapAdapter
    private lateinit var layoutManager : GridLayoutManager

    //테스트중.
    lateinit var bottomconst:ConstraintLayout
    private lateinit var mBundleRecyclerViewState :Bundle
    private lateinit var mRecyclerview :RecyclerView

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

    //paging
    private var start = 0 //페이징 시작 위치
    private val pageItems = 20  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함

    // 어댑터와 뷰 연결
    val mapInterListener = object : mapInter{
        override fun itemClick(id: Int){ //  맵리스트플래그먼트(하단플래그먼트) 리사이클러뷰 아이템 클릭시
            presenter.openPhoto(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logd(TAG, "onCreate")

        //맵리스트플래그먼트(하단플래그먼트)의 리사이클러뷰 생성
        layoutManager = GridLayoutManager(context!!, 2)

        //그리드레이아웃 매니저 spansize 다르게 하기(일반 아이템일 때는 한 열에 두 개씩, 로딩아이템일 때는 한 개씩)
        layoutManager.setSpanSizeLookup(object :GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                if(mapAdapter.getItemViewType(position) == 0){
                    return 1
                }else{
                    return 2
                }
            }
        })

        mapAdapter = MapAdapter(context!!, mapInterListener)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        logd(TAG, "onCreateView")

        val root = inflater.inflate(R.layout.fragment_map, container, false)

        bottomconst = root.findViewById<ConstraintLayout>(R.id.frag_list_map_f)

        mRecyclerview = root.findViewById(R.id.recycler_maplist_f)

        configureBackdrop()  //바텀시트 처리


        if(!::mapView.isInitialized){ //처음 생성될 때 빼고는 다시 구글맵을 초기화하지 않는다.
            val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.frag_googlemap_map_f) as SupportMapFragment
            mapFragment.getMapAsync(this)
            mapView = mapFragment.view!!
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        logd(TAG, "onActivityCreated")

        super.onActivityCreated(savedInstanceState)
        init()

        // 리사이클러뷰 끝까지 스크롤 할 때 리스너
        recycler_maplist_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recycler_maplist_f.canScrollVertically(1)){
                    if(selectedItems!=null && !pageLoading){
                        pageLoading = true //페이지 로딩중 중복 입력 방지

                        if(start < selectedItems!!.size){ //가져올 아이템이 남아있으면 실행

                            mapAdapter.addLoadingItem() //로딩아이템 생성 생성

                            Handler().postDelayed({
                                getPagedItems()
                            }, 600) //로딩 주기 0.6s
                        }

                    }
                }
            }
        })
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
         if (selectedItems != null) {
           text_spotnumber_maplist_f.text = selectedItems!!.size.toString()
         }


        mRecyclerview.layoutManager = layoutManager
        mRecyclerview.adapter = mapAdapter


    }

    override fun onResume() {
        super.onResume()

        logd(TAG, "onResume")

        if (::mBundleRecyclerViewState.isInitialized) {
            val listState : Parcelable = mBundleRecyclerViewState.getParcelable("recycler_state")
            logd(TAG, "mRecyclerview : "+ mRecyclerview)

                mRecyclerview.layoutManager!!.onRestoreInstanceState(listState)
        }
    }

    override fun onPause(){
        logd(TAG, "onPause")
        super.onPause()
        mBundleRecyclerViewState = Bundle()
        val listState =   mRecyclerview.layoutManager!!.onSaveInstanceState()
        mBundleRecyclerViewState.putParcelable("recycler_state", listState)
    }




    override fun onStop() {
        logd(TAG, "onStop")
        super.onStop()
        presenter.setLastPosition(mMap.cameraPosition) //마지막 구글맵 위치(위도,경도,줌) shared에 저장

        //현재 내 위치 받아오기 제거
        mFusedLocationClient.removeLocationUpdates(locationCallback)
        mylocation = null


        mRecyclerview.layoutManager = null
    }


    override fun onDestroyView() {
        logd(TAG, "onDestroyView")
        super.onDestroyView()
    }


    private fun configureBackdrop() { //바텀시트 처리

        logd(TAG, "configureBackdrop()")

        bottomconst?.let {

            logd(TAG, "frag_list_map_f")
            // Get the BottomSheetBehavior from the fragment view
            BottomSheetBehavior.from(it)?.let { bsb ->
                logd(TAG, "BottomSheetBehavior")
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                bottomconst.img_updown_maplist_f.setOnClickListener {
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
        img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        imgbtn_mylocation_map_f.isEnabled =false
        if(::mMap.isInitialized) {
            mMap.uiSettings.setAllGesturesEnabled(false)
        }
    }

    fun bottomCollapsed(){ //맵리스트플래그먼트(하단플래그먼트)가 내려가있을 때 일어나는 일
        img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
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

    inner class MapAdapter(val context: Context, val  mapInterListener:mapInter):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private var itemsList = ArrayList<MapCluster>()

        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM){ //아이템일 때 아이템뷰홀더 선택
                val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
                return ItemViewHolder(view)
            }else{ //로딩일 때 로딩뷰홀더 선택
                val view =  LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false)
                return LoadingViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        fun addPageItem(mapItem : MapCluster){
            itemsList.add(mapItem)
        }

        fun addLoadingItem(){
            isLoadingAdded = true
            add(MapCluster(0.0,0.0, "",0))
        }

        fun removeLoadingItem(){
            isLoadingAdded = false
            val position = itemsList.size -1
            val item = getItem(position)

            if(item != null){
                itemsList.remove(item)
                notifyItemRemoved(position)
            }
        }

        override fun getItemViewType(position: Int): Int {
            if(position==itemsList.size-1 && isLoadingAdded){
                return LOADING
            }else return ITEM
        }

        fun add(mapItem: MapCluster){
            itemsList.add(mapItem)
            notifyItemInserted(itemsList.size-1)
        }

        fun getItem(position: Int):MapCluster{
            return itemsList.get(position)
        }
        //=========================================================

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM) {
                val holder :ItemViewHolder = holder as ItemViewHolder
                itemsList[position].let {
                    Glide.with(holder.itemView.context)
                        .load(it.posts_image)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.photo)
                }

                holder.itemView.setOnClickListener {
                    mapInterListener.itemClick(itemsList[position].id)
                }
            }
        }

        inner class ItemViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }

    }



    override fun onMapReady(map: GoogleMap){ //구글맵이 로딩 되었을 때
        mMap = map

        mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기(회전 불가)

        mMap.setMinZoomPreference(8f) //지도 최대 축소 지정

        setClusterManager() //클러스터 세팅

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

            //클러스터 순서 테스트중....
            logd("clustertesting", "----------------------------------------------------")
            logd("clustertesting", "size  : "+cluster!!.items.size.toString())
            logd("clustertesting", "first : " +cluster!!.items.iterator().next())
            logd("clustertesting", "all   : " +cluster.items)


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
        text_spotnumber_maplist_f.text = cluster!!.size.toString()

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


        val clusterMapItems = ArrayList<MapCluster>()
        clusterMapItems.addAll(cluster!!.items)

        selectedItems = clusterMapItems //선택된 아이템 변경.

        mapAdapter.clearItemsAdapter()

        //새로운 클러스터를 선택할 때, 리사이클러뷰에도 아이템을 새로 불러온다. 페이징 시작 다시 0부터
        start = 0
        getPagedItems()

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


    override fun addItems(mapItems:ArrayList<MapCluster>){

        selectedItems = mapItems //선택된 아이템 변경.

        clusterManager.clearItems() //클러스터 삭제
        clusterManager.addItems(selectedItems)
        clusterManager.cluster()

        text_spotnumber_maplist_f.text = clusterManager.algorithm.items.size.toString()

        //위치를 새롭게 했을 때, 리사이클러뷰에도 아이템을 새로 불러온다. 페이징 시작 다시 0부터
        mapAdapter.clearItemsAdapter()
        start = 0
        getPagedItems()


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

    private fun getPagedItems(){ //페이징으로 나눠서 아이템 추가

       logd(TAG, "페이징, selecteditem size : "+selectedItems!!.size)

        if(start==0){
           recycler_maplist_f.scrollToPosition(0) // 시작점이 0이면 맨위로 고정
        }else{
            mapAdapter.removeLoadingItem()  //그 외에는 로딩아이템 제거
        }

        var end = start + pageItems
        if(end>selectedItems!!.size){ //끝 지점이 전체사이즈보다 크면
            end = selectedItems!!.size //전체 사이즈가 끝지점
        }

        if(start < selectedItems!!.size){
            for(i in start..end-1){
                mapAdapter.addPageItem(selectedItems!![i]) //시작점부터 끝지점 전까지 리사이클러뷰에 아이템 추가
            }
            start = start + pageItems //시작점 변경.
        }

       mapAdapter.notifyDataSetChanged()
       pageLoading = false
    }

}





