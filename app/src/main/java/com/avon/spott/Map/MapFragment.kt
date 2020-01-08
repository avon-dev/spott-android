package com.avon.spott.Map


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.MapItem
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.LocationListener
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
import com.google.maps.android.data.Renderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_list.*
import java.util.*


class MapFragment : Fragment() , MapContract.View, View.OnClickListener, OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MapItem>, ClusterManager.OnClusterItemClickListener<MapItem>{

    private lateinit var mapPresenter: MapPresenter
    override lateinit var presenter: MapContract.Presenter

    lateinit var childfragment :Fragment

    private lateinit var mMap : GoogleMap
    private lateinit var mapView:View

    private lateinit var clusterManager: ClusterManager<MapItem>

    private lateinit var  mCustomClusterItemRenderer : PhotoRenderer

    private var selectedMarker: Marker? = null //선택한 마커
    private var selectedMarkerView : View? = null //선택했던 마커뷰
    private var selectedCluster:Cluster<MapItem>? = null //선택한 아이템

    val mapInterListener = object : mapInter{
        override fun itemClick(){
            presenter.openPhoto()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        //바텀시트 처리
        configureBackdrop()

        //----------------임시 테스트----------------
        root.btn_find_map_f.setOnClickListener{

            clusterManager.clearItems()
            clusterManager.cluster()

            clusterManager = ClusterManager<MapItem>(context, mMap)

            mCustomClusterItemRenderer = PhotoRenderer()
            clusterManager!!.renderer = mCustomClusterItemRenderer
            clusterManager!!.renderer.setAnimation(false)



            mMap.setOnCameraIdleListener(clusterManager)
            mMap.setOnMarkerClickListener(clusterManager)
            mMap.setOnInfoWindowClickListener(clusterManager)
            clusterManager!!.setOnClusterClickListener(this)
            clusterManager!!.setOnClusterItemClickListener(this)

            addItems()

            clusterManager.cluster()
        }
        //-----------------------------------------------

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
        recycler_map_f.adapter = MapAdapter(context!!, mapInterListener)
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

    }


   var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

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
//                        bsb.state = BottomSheetBehavior.STATE_EXPANDED
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
                            BottomSheetBehavior.STATE_HIDDEN -> {
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                               bottomExpanded()
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                               bottomCollapsed()
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_SETTLING -> {
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })

            }


        }
    }

    fun bottomExpanded(){
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        imgbtn_mylocation_map_f.isEnabled =false
        btn_find_map_f.isEnabled =false
    }

    fun bottomCollapsed(){
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
        imgbtn_mylocation_map_f.isEnabled=true
        btn_find_map_f.isEnabled =true
    }

    override fun onClick(v: View?) {
        when(v?.id){
        }
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_mapFragment_to_photo)
    }


    interface mapInter{
        fun itemClick()
    }

    inner class MapAdapter(val context: Context, val  mapInterListener:mapInter):RecyclerView.Adapter<MapAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 30
        }

        override fun onBindViewHolder(holder: MapAdapter.ViewHolder, position: Int) {

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
            }else{
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }
            //---------------------------------------------------------------------------------------------------

            holder.itemView.setOnClickListener{
                mapInterListener.itemClick()
            }

        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }


    }

    override fun onMapReady(map: GoogleMap){
        mMap = map
        mMap.uiSettings.isRotateGesturesEnabled = false //지도 방향 고정시키기
        val defaultlocation = LatLng(37.56668, 126.97843)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultlocation, 12f))


        //클러스트링
        clusterManager = ClusterManager<MapItem>(context, mMap)

        mCustomClusterItemRenderer = PhotoRenderer()
        clusterManager!!.renderer = mCustomClusterItemRenderer

        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)
        clusterManager!!.setOnClusterClickListener(this)
        clusterManager!!.setOnClusterItemClickListener(this)

        addItems()

        clusterManager.cluster()


    }


    /* 사진이미지가 들어있는 풍선 모양 마커 비트맵을 만드는 함수 */
    private fun getMarkerBitmapFromView(view:View, bitmap: Bitmap, size:Int, selected:Boolean): Bitmap{
        val markerImageView = view.findViewById(R.id.img_photo_photo_m) as ImageView
        val countTextView = view.findViewById(R.id.text_count_photo_m) as TextView
        val markerCardView:CardView = view.findViewById(R.id.card_photo_m) as CardView
        val markerTriangleView = view.findViewById(R.id.view_triangle_photo_m) as View

        //클러스터면 카운트 텍스트 보이게, 클러스터아이템이면 사라지게
        if(size<2){
            countTextView.visibility = View.INVISIBLE
        }else{
            countTextView.visibility = View.VISIBLE
        }
        countTextView.text = size.toString()


        if(selected){ //선택된 클러스터
          markerCardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.markerSelected))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_select_24dp)
        }else{ //선택되지 않은 클러스터
          markerCardView.setCardBackgroundColor(ContextCompat.getColor(context!!, R.color.text_white))
            markerTriangleView.setBackgroundResource(R.drawable.ic_signal_wifi_4_bar_white_24dp)
        }

        markerImageView.setImageBitmap(bitmap)

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(
            0,
            0,
            view.getMeasuredWidth(),
            view.getMeasuredHeight()
        )
        println("뷰의 크기는?? " + view.getMeasuredWidth())

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


    /////클러스터 렌더링 테스트
    private inner class PhotoRenderer:DefaultClusterRenderer<MapItem>(context!!, mMap, clusterManager){

        val customMarkerView:View

        private val mIconGenerator = IconGenerator(context)
        private val mClusterIconGenerator = IconGenerator(context)

        private val mImageView: ImageView
        private val mDimension: Int

        private lateinit var icon: Bitmap


        init{
            customMarkerView = LayoutInflater.from(context).inflate(R.layout.marker_photo, null)
            mClusterIconGenerator.setContentView(customMarkerView)

            mImageView = ImageView(context)
            mDimension = 50
            mImageView.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
            val padding = 2
            mImageView.setPadding(padding, padding, padding, padding)
            mIconGenerator.setContentView(mImageView)

        }


        override fun onBeforeClusterItemRendered(item: MapItem?, markerOptions: MarkerOptions?) {
            // Draw a single person.
            println("1. 온비포클러스터아이템렌덜드")

            icon = mIconGenerator.makeIcon()
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))

        }

        override fun onClusterItemRendered(clusterItem: MapItem?, marker: Marker?) {
            println("2. 온비포클러스터아이템렌덜드")

            selectedMarker = null
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED

                Glide.with(context!!)
                    .asBitmap()
                    .load(clusterItem!!.imgUrl)
                    .fitCenter()
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                            marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    customMarkerView,
                                    bitmap, 1,false)))
                        }
                    })

        }


        override fun onBeforeClusterRendered(cluster: Cluster<MapItem>?,markerOptions: MarkerOptions?) {
            // Draw multiple people.
            println("3. 온비포클러스터렌덜드")

            icon = mClusterIconGenerator.makeIcon(cluster!!.size.toString())
            markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun onClusterRendered(cluster: Cluster<MapItem>?, marker: Marker?) {
            selectedMarker = null
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED

            println("4. 온클러스터렌덜드")

            //첫번째 아이템 선택
            val firstItem = cluster!!.items.iterator().next()

                Glide.with(context!!)
                    .asBitmap()
                    .load(firstItem.imgUrl)
                    .fitCenter()
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            marker!!.setIcon(
                                BitmapDescriptorFactory.fromBitmap(
                                    getMarkerBitmapFromView(
                                        customMarkerView,
                                        bitmap, cluster.size, false
                                    )
                                )
                            )
                        }

                    })

        }

        override fun shouldRenderAsCluster(cluster: Cluster<MapItem>?): Boolean {
            println("5. should랜더 as")
            // Always render clusters.
            return cluster!!.size > 1
        }

    }

    //사진 한장 마크를 누를때
    override fun onClusterItemClick(item: MapItem?): Boolean {
        Toast.makeText(context, "item : "+item!!.imgUrl, Toast.LENGTH_SHORT).show()

        presenter.openPhoto()

        return true
    }

    override fun onClusterClick(cluster: Cluster<MapItem>?): Boolean {
        Toast.makeText(context, "size : " + cluster!!.size, Toast.LENGTH_SHORT).show()

        if(selectedMarker !=null){ //전에 선택했던 마커는 다시 하얀색으로 바꾸기
            println("바껴라")

            Glide.with(context!!)
                .asBitmap()
                .load(selectedCluster!!.items.iterator().next().imgUrl)
                .fitCenter()
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                        selectedMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(
                                selectedMarkerView!!,
                                bitmap, selectedCluster!!.size,false)))
                    }
                })
        }

        //첫번째 아이템 선택
        val firstItem = cluster!!.items.iterator().next()

        selectedMarker = mCustomClusterItemRenderer.getMarker(cluster)
        selectedCluster = cluster
        selectedMarkerView = mCustomClusterItemRenderer.customMarkerView




        selectedMarker = mCustomClusterItemRenderer.getMarker(cluster)
        selectedMarkerView = mCustomClusterItemRenderer.customMarkerView

        Glide.with(context!!)
            .asBitmap()
            .load(firstItem.imgUrl)
            .fitCenter()
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    bitmap: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    selectedMarker!!.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(
                                selectedMarkerView!!,
                                bitmap, cluster.size, true
                            )
                        )
                    )
                }

            })

        val quarterLat =
            (mMap.projection.visibleRegion.latLngBounds.northeast.latitude-mMap.projection.visibleRegion.latLngBounds.southwest.latitude)/4

        println("노스이스트 latitude? " +  mMap.projection.visibleRegion.latLngBounds.northeast.latitude)
        println("사우스웨스트 latitude? " +  mMap.projection.visibleRegion.latLngBounds.southwest.latitude)
        println("쿼터렛은? " + quarterLat)

        val newPositon = LatLng(cluster.position.latitude-quarterLat ,cluster.position.longitude)

        val center : CameraUpdate = CameraUpdateFactory.newLatLng(newPositon)
        mMap.animateCamera(center) //카메라 이동

        //클러스터 선택시 리스트플래그먼트 반만 올라오게
        mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_HALF_EXPANDED

        return true
    }





    ///==============구글맵 더미 데이터============================================
    private fun addItems(){  //구글맵 더미 마커
        clusterManager!!.addItem(MapItem(LatLng(37.565597, 126.978009), "",
            "https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.564920, 126.925100), "",
            "https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.547759, 126.922873), "",
            "https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.504458, 126.986861), "",
            "https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg"))

        clusterManager!!.addItem(MapItem(LatLng(37.566597, 126.968009), "",
            "https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.563920, 126.955100), "",
            "https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.545759, 126.942873), "",
            "https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg"))
        clusterManager!!.addItem(MapItem(LatLng(37.514458, 126.996861), "",
            "https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg"))


    }
    //////=======================더미데이터 끝==============================================


    //===================테스트용 랜덤 위치 생성=================================
     private fun position():LatLng{
        return LatLng(random(37.489324, 37.626495), random(126.903712, 127.096659))
    }

    private fun random(min:Double, max:Double):Double{
        return min+(max-min)* Random().nextDouble()
    }
    //======================================================================



}









