package com.avon.spott.Mypage

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.*
import com.avon.spott.AddPhoto.AddPhotoActivity
import com.avon.spott.Data.MapCluster
import com.avon.spott.EditMyinfo.EditMyInfoActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MypageFragment : Fragment(), MypageContract.View, View.OnClickListener, OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MapCluster>, ClusterManager.OnClusterItemClickListener<MapCluster>{

    private val TAG = "forMypageFragment"

    private val SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage.jpg"

    companion object{
        var selectedMarkerMypage : Marker? = null //선택한 마커

        lateinit var mapRecyclerView: RecyclerView  //Map recyclerview
        var mapRecyclerViewShow = false //마이페이지 맵 리사이클러뷰 visible 여부

        var mypageChange = false //내가 작성한 사진이 변화가 있는 지 여부
    }

    var Mypageselectgrid = true //마이페이지 그리드 탭이 보이는지 여부

    private lateinit var mypagePresenter: MypagePresenter
    override lateinit var presenter: MypageContract.Presenter

    //googlemap
    private lateinit var mMap : GoogleMap
    private lateinit var mapView : View

    //Grid recyclerview
    private lateinit var mypageAdapter: MypageAdapter
    private lateinit var layoutManager : GridLayoutManager

    //Map recyclerview
    private lateinit var mypageMapAdapter: MypageMapAdapter
    private lateinit var maplayoutManager: LinearLayoutManager

    //googlemap clustering
    private lateinit var clusterManager : ClusterManager<MapCluster>
    private lateinit var  mCustomClusterItemRenderer : PhotoRenderer

    //전에 선택된 마커 처리
    private var selectedMarkerView : View? = null //선택했던 마커뷰
    private var selectedCluster : Cluster<MapCluster>? = null //선택한 클러스터
    private var selectedItems : ArrayList<MapCluster>? = null//선택된 아이템

    //서버에서 불러온 내 전체 아이템들
    private var wholeItems:ArrayList<MapCluster>? = null

    //유저의 닉네임과 아이디
    private var userNickname:String? = null
    private var userPhoto:String? = null

    //공개 비공개 여부
    private var isPublic = true

    //확인 안 한 알림 카운트
    private var noticount = 0

    private var checkInit = false

    var mPhotoPath: Uri? = null

    val mypageInterListener = object : mypageInter{
        override fun itemClick(id:Int){
            presenter.openPhoto(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Grid recyclerview 용
        layoutManager = GridLayoutManager(context!!, 3)
        mypageAdapter = MypageAdapter(context!!, mypageInterListener)

        //Map recyclerview 용
        maplayoutManager = LinearLayoutManager(context!!, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
            false)
        mypageMapAdapter = MypageMapAdapter(context!!, mypageInterListener)

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_mypage, container, false)

        if(!::mapView.isInitialized){ //처음 생성될 때 빼고는 다시 구글맵을 초기화하지 않는다.
            val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.frag_googlemap_mypage_f) as SupportMapFragment
            mapFragment.getMapAsync(this)
            mapView = mapFragment.view!!
        }

        mapRecyclerView = root.findViewById(R.id.recycler_map_mypage_f)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        recycler_grid_mypage_f.layoutManager  = layoutManager
        recycler_grid_mypage_f.adapter = mypageAdapter

        mapRecyclerView.layoutManager = maplayoutManager
        mapRecyclerView.adapter = mypageMapAdapter

        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_photo_white_24dp))
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_location_on_white_24dp))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab!!.position){
                    0 -> {
                        const_grid_mypage_f.visibility = View.VISIBLE
                        const_map_mypage_f.visibility = View.GONE
                        Mypageselectgrid = true
                    }
                    1 -> {
                        const_grid_mypage_f.visibility = View.GONE
                        const_map_mypage_f.visibility = View.VISIBLE
                        Mypageselectgrid = false
                    }
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })


        if(Mypageselectgrid){
           tabLayout.getTabAt(0)!!.select()
            const_grid_mypage_f.visibility = View.VISIBLE
            const_map_mypage_f.visibility = View.GONE
        }else{
            tabLayout.getTabAt(1)!!.select()
            const_grid_mypage_f.visibility = View.GONE
            const_map_mypage_f.visibility = View.VISIBLE
        }

        if(mapRecyclerViewShow){
            mapRecyclerView.visibility = View.VISIBLE
        }else{
            mapRecyclerView.visibility = View.GONE
        }

        if(userNickname!=null){
            setUserInfo(userNickname!!, userPhoto, isPublic)
        }

        swiperefresh_mypager_f.setOnRefreshListener {
            Handler().postDelayed({
                presenter.getMyphotos(getString(R.string.baseurl))
            }, 600) //로딩 주기
        }

        swiperefresh_mypager_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))


    }

    override fun onStart() {
        super.onStart()

        if(!checkInit){
            // 뒤로가기만 보이게
            controlToolbar(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE)
            mToolbar.visibility = View.VISIBLE
        }else{
            // 툴바 유저이미지, 유저닉네임, 알람, 메뉴 보이게
            controlToolbar(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE)
            mToolbar.visibility = View.VISIBLE
        }


        setNotiCount(noticount)

        if( wholeItems!=null &&  wholeItems!!.size == 0){ //서버에서 불러왔던 사진아이템 사이즈가 0이면 사진없음 문구 보이게
            text_nophoto_mypage_f.visibility = View.VISIBLE
        }

        if(mypageChange){ //마이페이지에 변화가 있으면 새로 불러온다.
            text_nophoto_mypage_f.visibility = View.GONE
            mypageChange = false
            clearMypage()

            presenter.getMyphotos(getString(R.string.baseurl))
        }else if(checkInit){
            presenter.getNotiCount(getString(R.string.baseurl)) // 새로운 알림을 가져옴
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        recycler_grid_mypage_f.layoutManager = null
        mapRecyclerView.layoutManager = null

        if(mapRecyclerView.visibility == View.VISIBLE){
            mapRecyclerViewShow = true
        }else if(mapRecyclerView.visibility == View.GONE){
            mapRecyclerViewShow = false
        }


    }

    fun init(){
        mypagePresenter = MypagePresenter(this)

        mToolbar.frame_noti_toolbar.setOnClickListener(this)
        mToolbar.img_menu_toolbar.setOnClickListener(this)
        floatimgbtn_addphoto_mypage.setOnClickListener(this)

    }

    override fun onClusterItemClick(item: MapCluster?): Boolean { //클러스터아이템(사진 한 장) 눌렀을 때
        presenter.openPhoto(item!!.id)
        return true
    }

    override fun onClusterClick(cluster: Cluster<MapCluster>?): Boolean {


            if(selectedMarkerMypage != null){  //전에 선택했던 마커는 다시 하얀색으로 바꾸기
                val sortItmes = selectedCluster!!.items.sortedByDescending { mapCluster: MapCluster? -> mapCluster!!.id }
                val firstItem = sortItmes[0]  //첫번째 아이템 선택

                Glide.with(context!!)
                    .asBitmap()
                    .load(firstItem.posts_image)
                    .fitCenter()
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                            try {
                                selectedMarkerMypage!!.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        // 전에 선택했던 클러스터 아이템의 이미지를 넣은 클러스터 마커의 비트맵(테두리 하얀색 마커)을 만든다.
                                        getMarkerBitmapFromView(
                                            selectedMarkerView!!,
                                            bitmap,
                                            selectedCluster!!.size,
                                            false,
                                            context!!
                                        )
                                    )
                                )
                                newCluster(cluster) // 새로 선택된 클러스터의 이미지를 넣은 클러스터 마커의 비트맵(테두리 파란색 마커)을 만든다.
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }else if(selectedMarkerMypage == null) { // 전에 선택된 클러스터가 없다면
            newCluster(cluster) // 바로 새로 선택된 클러스터의 이미지를 넣은 클러스터 마커의 비트맵(테두리 파란색 마커)을 만든다.
        }
        return true
    }

    private fun newCluster(cluster: Cluster<MapCluster>?){

        maplayoutManager.scrollToPosition(0)

        if(selectedCluster==cluster&& selectedMarkerMypage!=null){

            mapRecyclerView.isEnabled = false
            animSlide(context!!, mapRecyclerView, false)

            selectedMarkerMypage = null
        }else {

            val sortItmes = cluster!!.items.sortedByDescending { mapCluster: MapCluster? -> mapCluster!!.id }

            val firstItem = sortItmes[0] //첫번째 아이템 선택

            selectedCluster = cluster
            selectedMarkerMypage = mCustomClusterItemRenderer.getMarker(cluster)
            selectedMarkerView = mCustomClusterItemRenderer.customMarkerView

            Glide.with(context!!)
                .asBitmap()
                .load(firstItem.posts_image)
                .fitCenter()
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        try {
                        selectedMarkerMypage!!.setIcon(
                            BitmapDescriptorFactory.fromBitmap(
                                getMarkerBitmapFromView(
                                    selectedMarkerView!!,
                                    bitmap,
                                    cluster.size,
                                    true,
                                    context!!
                                )
                            )
                        )
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })

            val clusterMapItems = ArrayList<MapCluster>()
            clusterMapItems.addAll(sortItmes)

            selectedItems = clusterMapItems //선택된 아이템 변경
            mypageMapAdapter.clearItemsAdapter()
            mypageMapAdapter.addItemsAdapter(selectedItems!!)
            mypageMapAdapter.notifyDataSetChanged()

            mapRecyclerView.isEnabled = true
            animSlide(context!!, mapRecyclerView, true)


            val center: CameraUpdate = CameraUpdateFactory.newLatLng(cluster.position)
            mMap.animateCamera(center, 400, null)
        }

    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        clusterManager = ClusterManager<MapCluster>(context, mMap)

        mCustomClusterItemRenderer = PhotoRenderer(context!!, mMap, clusterManager, false)
        clusterManager.renderer = mCustomClusterItemRenderer
        clusterManager.renderer.setAnimation(false)

        mMap.setOnMarkerClickListener(clusterManager)
        mMap.setOnInfoWindowClickListener(clusterManager)
        mMap.setOnCameraIdleListener(clusterManager)
        clusterManager.setOnClusterClickListener(this)
        clusterManager.setOnClusterItemClickListener(this)

        presenter.getMyphotos(getString(R.string.baseurl))

    }

    override fun clearAdapter(){
        if(swiperefresh_mypager_f.isRefreshing){
            clearMypage()
            swiperefresh_mypager_f.isRefreshing = false

        }
    }

    override fun addItems(mypageItems: ArrayList<MapCluster>) { //프레젠터에서 넘어온 아이템을 클러스터와 어댑터에 뿌림.
        logd(TAG, "넘어온 아이템은 " + mypageItems)
        wholeItems = mypageItems

        clusterManager.addItems(mypageItems)
        clusterManager.cluster()

        mypageAdapter.addItemsAdapter(mypageItems)
        mypageAdapter.notifyDataSetChanged()

        if(!checkInit){
            // 툴바 유저이미지, 유저닉네임, 알람, 메뉴 보이게
            controlToolbar(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE)
            mToolbar.visibility = View.VISIBLE
        }

        checkInit = true
    }

    override fun noPhoto(){
        text_nophoto_mypage_f.visibility = View.VISIBLE
    }

    override fun movePosition(latLng: LatLng, zoom: Float) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun showPhotoUi(id:Int) {//PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_mypageFragment_to_photo, bundle)
    }

    override fun showAddPhotoUi(mFilePath : String, mCropPath: String) {
        val nextIntent = Intent(context, AddPhotoActivity::class.java)
        nextIntent.putExtra("cropPhoto",  mCropPath)
        nextIntent.putExtra("photo", mFilePath)
        startActivity(nextIntent)
    }

    override fun showAlarmUi() {
        findNavController().navigate(R.id.action_mypageFragment_to_alarmFragment)
    }

    override fun showEditMyInfoUi() {
        startActivity(Intent(context, EditMyInfoActivity::class.java))
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.frame_noti_toolbar -> {presenter.openAlarm()}
            R.id.floatimgbtn_addphoto_mypage -> {
                presenter.clickAddPhoto()
            }
            R.id.img_menu_toolbar -> {
                showMenuDialog()
            }
        }
    }

    interface mypageInter{
        fun itemClick(id:Int)
    }

    inner class MypageAdapter(val context: Context, val mypageInterListener:mypageInter):RecyclerView.Adapter<MypageAdapter.ViewHolder>(){

        private var itemsList = ArrayList<MapCluster>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MypageAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(mypageItems:ArrayList<MapCluster>){
            itemsList.addAll(mypageItems)
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        override fun onBindViewHolder(holder: MypageAdapter.ViewHolder, position: Int) {

           itemsList[position].let{
             Glide.with(holder.itemView.context)
                 .load(it.posts_image)
                 .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                 .error(android.R.drawable.stat_notify_error)
                 .into(holder.photo)


             holder.itemView.setOnClickListener {
                 mypageInterListener.itemClick(itemsList[position].id)
             }
           }

        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

    }

    inner class MypageMapAdapter(val context: Context, val mypageInterListener:mypageInter):RecyclerView.Adapter<MypageMapAdapter.ViewHolder>() {
        private var itemsList = ArrayList<MapCluster>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MypageMapAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_photo_square_mypagemap, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        fun addItemsAdapter(mapItems : ArrayList<MapCluster>){
            logd(TAG, "addItemsAdapter!!!!!")
            itemsList.addAll(mapItems)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            itemsList[position].let{
                logd(TAG, "onBindViewHolder!!!! " + it.posts_image)
                Glide.with(holder.itemView.context)
                    .load(it.posts_image)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)


                holder.itemView.setOnClickListener {
                    mypageInterListener.itemClick(itemsList[position].id)
                }
            }
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_squareMypageMap_i) as ImageView
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
                   mPhotoPath = data.getData()!!
                    logd(TAG, "photopath : " + mPhotoPath)

                    val options = UCrop.Options()
                    options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.SCALE)
                    options.setToolbarTitle("")
                    options.setToolbarCropDrawable(R.drawable.ic_arrow_forward_black_24dp)
                    options.setActiveControlsWidgetColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    options.setStatusBarColor(ContextCompat.getColor(context!!, R.color.bg_black))
                    options.setCompressionQuality(100)
                    options.setMaxBitmapSize(10000)
                    options.setAspectRatioOptions(1,
//                        AspectRatio("16 : 9", 16f, 9f),
                        AspectRatio("4 : 3", 4f, 3f),
                        AspectRatio("1 : 1", 1f, 1f),
                        AspectRatio("3 : 4", 3f, 4f)
//                        AspectRatio("9 : 16", 9f, 16f)
                    )

                    /* 현재시간을 임시 파일 이름에 넣는 이유 : 중복방지
                    / (안넣으면 AddPhotoActivity의 이미지뷰에 다른 사진 보여진다.) */
                    val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
                    UCrop.of(mPhotoPath!!, Uri.fromFile(File(context!!.cacheDir, timeStamp+SAMPLE_CROPPED_IMAGE_NAME)))
                        .withMaxResultSize(resources.getDimension(R.dimen.upload_width).toInt(),
                            resources.getDimension(R.dimen.upload_heigth).toInt())
                        .withOptions(options)
                        .start(context!!, this)

                }
            }else if(requestCode == UCrop.REQUEST_CROP){
                    var mCropPath: Uri? = UCrop.getOutput(data)
                    logd(TAG, "croppath : " + mCropPath)
                    presenter.openAddPhoto(mPhotoPath.toString(), mCropPath.toString())
                }
        }
        if(resultCode == UCrop.RESULT_ERROR){
            logd(TAG, "error : Ucrop result error")
        }
    }

    override fun setUserInfo(nickname:String, photo:String?, isPublic:Boolean){
        userNickname = nickname
        userPhoto = photo

        if(photo==null){
            mToolbar.img_profile_toolbar.setImageResource(R.drawable.img_person)
        }else{
            Glide.with(this)
                .load(photo)
                .into(mToolbar.img_profile_toolbar)
        }

        mToolbar.text_name_toolbar.text=nickname
        showPublic(isPublic)
    }

    private fun clearMypage(){
        // 그리드 리사이클러뷰 클리어
        mypageAdapter.clearItemsAdapter()
        mypageAdapter.notifyDataSetChanged()

        //클러스터 클리어
        clusterManager.clearItems()
        clusterManager.cluster()

        //맵 리사이클러뷰 클리어 및 사라지게 처리
        mypageMapAdapter.clearItemsAdapter()
        mypageMapAdapter.notifyDataSetChanged()
        mapRecyclerView.visibility = View.GONE

        //선택했던 마커 없애기
        selectedMarkerMypage  = null
    }

    private fun showMenuDialog(){
        val builder = AlertDialog.Builder(context)

        val arrayList = ArrayList<String>()
        arrayList.add(getString(R.string.text_title_editmyinfo_a))
        arrayList.add(if(isPublic) getString(R.string.keep_private) else getString(R.string.keep_public))
        arrayList.add(getString(R.string.inquiry))
        arrayList.add(getString(R.string.text_terms_of_service))
        arrayList.add(getString(R.string.text_open_source_library))

        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, arrayList)
        val listener = object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when(which){
                    0 -> { // 내 정보 수정 눌렀을 때
                        presenter.openEditMyInfo()
                    }
                    1 -> { // 공개, 비공개 전환
                       presenter.changePublic(getString(R.string.baseurl), isPublic)
                    }
                    2 ->{ //문의하기 눌렀을 때
                        /** 메일 앱으로 연동하거나
                         *  어드민사이트로 보낼 예정*/
                        inquire(context!!)
                    }
                    3->{ //이용약관 눌렀을 때

                    }
                    4->{ //오픈소스 라이브러리 눌렀을 때

                    }
                }
            }
        }

        builder.setAdapter(adapter, listener)
        builder.show()

    }

    override fun showPublic(isPublic: Boolean){
        this.isPublic = isPublic
        const_private_mypage_f.visibility = if(isPublic) View.GONE else View.VISIBLE
    }

    override fun showErrorToast() {
        showToast(getString(R.string.server_connection_error))
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun setNotiCount(count: Int) {
//        val count = 0
//        showToast(count.toString())

        noticount = count

        mToolbar.text_noticount_toolbar.text = count.toString()

        var padding :Int
        if(count<10){
            padding = resources.getDimension(R.dimen.noti_count_1).toInt()
        }else if(count<100){
            padding = resources.getDimension(R.dimen.noti_count_2).toInt()
        }else{
            mToolbar.text_noticount_toolbar.text = "99+"
            padding = resources.getDimension(R.dimen.noti_count_3).toInt()
        }
        mToolbar.text_noticount_toolbar.setPadding(padding,0,padding,0)

        mToolbar.text_noticount_toolbar.visibility = if(count == 0) View.GONE else View.VISIBLE

    }



}





