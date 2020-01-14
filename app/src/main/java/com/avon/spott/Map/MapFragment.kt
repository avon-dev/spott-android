package com.avon.spott.Map


import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.Map
import com.avon.spott.Data.MapCluster
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_list.*
import java.util.*
import kotlin.collections.ArrayList

class MapFragment : Fragment() , MapContract.View, View.OnClickListener, OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MapCluster>, ClusterManager.OnClusterItemClickListener<MapCluster>{

    private val TAG = "forMapFragment"

    private lateinit var mapPresenter : MapPresenter
    override lateinit var presenter : MapContract.Presenter

    private lateinit var mapAdapter: MapAdapter

    lateinit var childfragment : Fragment
    lateinit var mBottomSheetBehavior : BottomSheetBehavior<View?>

    private lateinit var mMap : GoogleMap
    private lateinit var mapView : View

    private lateinit var clusterManager : ClusterManager<MapCluster>
    private lateinit var  mCustomClusterItemRenderer : PhotoRenderer

    private var selectedMarker : Marker? = null //선택한 마커
    private var selectedMarkerView : View? = null //선택했던 마커뷰
    private var selectedCluster : Cluster<MapCluster>? = null //선택한 클러스터
    private var selectedItems : ArrayList<Map>? = null//선택된 아이템

    private var initMapMoving : Boolean = true //처음 맵 로딩하는지 여부

    val mapInterListener = object : mapInter{
        override fun itemClick(id: Int){
            presenter.openPhoto(id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        //바텀시트 처리
        configureBackdrop()

        //----------------임시 테스트----------------
        root.btn_find_map_f.setOnClickListener{
            //새로 아이템을 불러오는 함수 넣어야함.

            btn_find_map_f.visibility = View.GONE
            text_nophoto_maplist_f.visibility = View.GONE

            clusterManager.clearItems()
            clusterManager.cluster()

            setClusterManager()

            sendCameraRange()
        }
        //-----------------------------------------------

        //------사진 없을 경우 테스트--------------------------
        root.imgbtn_mylocation_map_f.setOnClickListener{

                btn_find_map_f.visibility = View.GONE
                text_nophoto_maplist_f.visibility = View.GONE

                clusterManager.clearItems()
                clusterManager.cluster()

                setClusterManager()

                presenter.getNophoto()
            }
        //------------------------------------------------


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

        //---------------리사이클러뷰테스트 코드------------------------------
        val layoutManager = GridLayoutManager(context!!, 2)
        recycler_map_f.layoutManager = layoutManager

        mapAdapter = MapAdapter(context!!, mapInterListener)

        recycler_map_f.adapter = mapAdapter
        //-----------------------------------------------------------------
    }

    fun init(){
        mapPresenter = MapPresenter(this)
    }

    override fun onStart() {
        super.onStart()

        //툴바 안보이게
        mToolbar.visibility = View.GONE

        //바텀시트가 현재 어떤 상태인지 확인하고 해당 상태에 맞게 ui 처리
        if(mBottomSheetBehavior?.state == STATE_EXPANDED){
            bottomExpanded()
        }else{
            bottomCollapsed()
        }

        //선택했던 아이템 있을 경우 리스트플래그먼트의 리사이클러뷰와 전체 개수 텍스트 처리
        if(selectedItems!=null){

            mapAdapter.addItemsAdapter(selectedItems!!)
            mapAdapter.notifyDataSetChanged()

            text_spotnumber_maplist_f.text = selectedItems!!.size.toString()
        }

    }

    private fun configureBackdrop() {
        // Get the fragment reference
        childfragment = childFragmentManager?.findFragmentById(R.id.frag_list_map_f)!!

        childfragment?.let {
            // Get the BottomSheetBehavior from the fragment view
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

    fun bottomExpanded(){ //리스트플래그먼트가 올라와있을 때 일어나는 일
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        imgbtn_mylocation_map_f.isEnabled =false
        btn_find_map_f.isEnabled =false
        if(::mMap.isInitialized) {
            mMap.uiSettings.setAllGesturesEnabled(false)
        }
    }

    fun bottomCollapsed(){ //리스트플래그먼트가 내려가있을 때 일어나는 일
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
        imgbtn_mylocation_map_f.isEnabled=true
        btn_find_map_f.isEnabled =true
        if(::mMap.isInitialized){
            mMap.uiSettings.setAllGesturesEnabled(true)
            mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기
        }
    }

    override fun onClick(v: View?){
        when(v?.id){
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

        private var itemsList = ArrayList<Map>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(mapItems: ArrayList<Map>){
            itemsList = mapItems
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        override fun onBindViewHolder(holder: MapAdapter.ViewHolder, position: Int) {

            itemsList[position].let{
                     Glide.with(holder.itemView.context)
                    .load(it.imgUrl)
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
        presenter.getLastPosition() //카메라 포지션 옮기기. (처음실행 : 서울, 그외 : 최근에 봤던 곳)

        mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기(회전 불가)

        setClusterManager()

        sendCameraRange()


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
                .load(clusterItem!!.imgUrl)
                .fitCenter()
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(customMarkerView, bitmap, 1,false)))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        }


        override fun onBeforeClusterRendered(cluster: Cluster<MapCluster>?, markerOptions: MarkerOptions?) {
            //클러스터 만들어지기 전 임시로 나오는 아이콘 지정 -> empty bitmap
            markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun onClusterRendered(cluster: Cluster<MapCluster>?, marker: Marker?) {
            selectedMarker = null//선택된 클러스터 null로 만듦. -> 새로 클러스터링이 되면 선택했던 클러스터 없어지게함.
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED //리스트플래그먼트는 내려가게함.

            val firstItem = cluster!!.items.iterator().next()   //첫번째 아이템 선택

            Glide.with(context!!)
                .asBitmap()
                .load(firstItem.imgUrl)
                .fitCenter()
                .into(object :CustomTarget<Bitmap>(){
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?){
                        marker!!.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(customMarkerView,bitmap, cluster.size, false)))
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
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
            return true   //리스트플래그먼트 상태가 올라온 상태라면 클러스터 클릭안되게
        }
        presenter.openPhoto(item!!.id) //사진 한 장 클릭시 PhotoFragment로 이동
        return true
    }

    override fun onClusterClick(cluster: Cluster<MapCluster>?): Boolean { //클러스터(사진 여러장) 눌렀을 때 일어나는 일

        if(mBottomSheetBehavior?.state ==  BottomSheetBehavior.STATE_EXPANDED){
            //리스트플래그먼트 상태가 올라온 상태라면 클러스터 클릭안되게
            return true
        }


        if(selectedMarker !=null){ //전에 선택했던 마커는 다시 하얀색으로 바꾸기
            Glide.with(context!!)
                .asBitmap()
                .load(selectedCluster!!.items.iterator().next().imgUrl) //전에 선택했던 클러스터의 첫번째 이미지 url가져온다.
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
            .load(firstItem.imgUrl)
            .fitCenter()
            .into(object :CustomTarget<Bitmap>(){
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?){
                    selectedMarker!!.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(selectedMarkerView!!, bitmap, cluster.size, true)))
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })

        val quarterLat = //지금 보여지는 맵 위쪽과 아래쪽의 위도 차이를 구해서 나눔.
            (mMap.projection.visibleRegion.latLngBounds.northeast.latitude - mMap.projection.visibleRegion.latLngBounds.southwest.latitude)/6

        val newPositon = //클러스터의 위도에서 바로 위에 구한 위도차이를 빼줌
            LatLng(cluster.position.latitude - quarterLat, cluster.position.longitude)

        val center: CameraUpdate = CameraUpdateFactory.newLatLng(newPositon)
        mMap.animateCamera(center) //카메라 이동

        mapAdapter.clearItemsAdapter()

        val clusterMapItems = ArrayList<Map>()
        for(i in 0..selectedCluster!!.items.size-1){
            val item = cluster!!.items.iterator().asSequence().toList()
            logd(TAG, item.toString())
            clusterMapItems.add(Map(item[i].latLng.latitude,item[i].latLng.longitude, item[i].imgUrl, item[i] .id))
        }

        selectedItems = clusterMapItems //선택된 아이템 변경.

        mapAdapter.addItemsAdapter(clusterMapItems)
        mapAdapter.notifyDataSetChanged()

        //클러스터 선택시 리스트플래그먼트 반만 올라오게
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }


    override fun onStop() {
        super.onStop()
        presenter.setLastPosition(mMap.cameraPosition) //마지막 구글맵 위치(위도,경도,줌) shared에 저장
    }

    override fun addItems(mapItems:ArrayList<Map>){

        selectedItems = mapItems //선택된 아이템 변경.

        for(i  in 0..mapItems.size-1){
            clusterManager!!.addItem(MapCluster(LatLng(mapItems[i].lat, mapItems[i].lng), "",
                mapItems[i].imgUrl, mapItems[i].id))
        }

        clusterManager.cluster()
        //리스트플래그먼트에 총개수 넣음 (더미)
        childfragment.text_spotnumber_maplist_f.text = clusterManager.algorithm.items.size.toString()

        //리스트플래그먼트 리사이클러뷰에 아이템 추가
        mapAdapter.addItemsAdapter(mapItems)
        mapAdapter.notifyDataSetChanged()

        //하단 플레그먼트 내려가게
        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomCollapsed()
    }


    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun sendCameraRange(){ //현재 화면에 보여지는 위치 서버에 보냄.
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
                logd(TAG, "Camera movement has ended")
                if(!initMapMoving){ //처음 맵을 세팅할 때는 카메라 움직임이 끝나도 "이 지역에서 찾기"버튼 보여주지 않음.
                    btn_find_map_f.visibility = View.VISIBLE
                }
                initMapMoving = false

                clusterManager.cluster()
            }
        } )

    }
    override fun noPhoto(){ //카메라 범위에 해당하는 사진이 없을때
        text_spotnumber_maplist_f.text = "0"
        text_nophoto_maplist_f.visibility = View.VISIBLE
    }

}





