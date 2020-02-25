package com.avon.spott.Home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avon.spott.Data.HomeItem
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Utils.DateTimeFormatter.Companion.formatCreated
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment(), HomeContract.View, View.OnClickListener {

    private val TAG = "forHomeFragment"

    private lateinit var homePresenter: HomePresenter
    override lateinit var presenter: HomeContract.Presenter

    private lateinit var homeAdapter: HomeAdapter
    private lateinit var layoutManager : StaggeredGridLayoutManager

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 20  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함
    override var hasNext: Boolean = false // 다음으로 가져올 페이지가 있는지 여부
    override var refreshTimeStamp:String = "" // 서버에서 페이지를 가지고오는 시간적 기준점(페이징 도중 사진이 추가될 때 중복됨을 방지)

    private var checkInit = false

    private var ACTION = 1003

    val homeInterListener = object :homeInter{
        override fun itemClick(id: Int) {
            presenter.openPhoto(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //StaggeredGridLayout 아이템 재배치 시킴
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        homeAdapter = HomeAdapter(context!!, homeInterListener)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

        recycler_home_f.layoutManager = layoutManager
        recycler_home_f.adapter = homeAdapter

        recycler_home_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recycler_home_f.canScrollVertically(1)){ // 맨 아래에서 스크롤 할 때
                    if(!pageLoading && hasNext){ //이미 페이징을 불러오는 동안 중복요청하지 않게하기위함. + 다음 가져올 페이지가 있는지 여부확인
                        logd("ScrollTEST","END")
                        pageLoading = true
                        homeAdapter.addPageLoadingItem() //리싸이클러뷰에 로딩아이템 생성 생성
                        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount-1)

                        Handler().postDelayed({
                            presenter.getPhotos(getString(R.string.baseurl), start, ACTION)
                        }, 400) //로딩 주기
                    }
                }
            }
        })

        swiperefresh_home_f.setOnRefreshListener{ // 스크롤 위로 올려서 리프레시 했을 때
            Handler().postDelayed({
                logd(TAG, "refreshed!!!")

                //페이징 시작위치와 시간 초기화
                start = 0
                refreshTimeStamp = ""

                presenter.getPhotos(getString(R.string.baseurl), start, ACTION)
            }, 600) //로딩 주기

        }

        if(!checkInit) {
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
            presenter.getToken(getString(R.string.baseurl), start, ACTION)

            checkInit = true
        }

        //스와이퍼리프레쉬 레이아웃 색상변경
        swiperefresh_home_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

    }


    override fun onStart() {
        super.onStart()
        //툴바 안보이게
        mToolbar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_home_f.layoutManager = null
    }

    fun init(){
        homePresenter = HomePresenter(this)
        img_search_home_f.setOnClickListener(this)
    }

    override fun showPhotoUi(id:Int) { //PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_homeFragment_to_photo, bundle)
    }

    override fun showSearchUi() {
        findNavController().navigate(R.id.action_homeFragment_to_search)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.img_search_home_f ->{presenter.openSearch()}
        }
    }


    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface homeInter{
        fun itemClick(id: Int)
    }

    override fun addItems(homeItemItems: ArrayList<HomeItem>){
        start = start + pageItems

        homeAdapter.addItemsAdapter(homeItemItems)
        homeAdapter.notifyDataSetChanged()

        pageLoading = false
    }

    override fun removePageLoading(){
        homeAdapter.removePageLoadingItem()
    }

    override fun clearAdapter(){
        if (swiperefresh_home_f.isRefreshing) {
            homeAdapter.clearItemsAdapter()
            homeAdapter.notifyDataSetChanged()
            swiperefresh_home_f.isRefreshing = false
        }
    }

    inner class HomeAdapter(val context: Context, val homeInterListner:homeInter) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private var itemsList = ArrayList<HomeItem>()

        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM) { //아이템일 때 아이템뷰홀더 선택
                val view = LayoutInflater.from(context).inflate(R.layout.item_camera, parent, false)
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

        fun addItemsAdapter(homeItemItems: ArrayList<HomeItem>){
            itemsList.addAll(homeItemItems)
        }

        //리싸이클러뷰 아래로 드래그시 페이징 로딩아이템 추가
        fun addPageLoadingItem() {
            isLoadingAdded = true
            addPage(HomeItem("",0))
        }

        fun removePageLoadingItem(){
            isLoadingAdded = false
            val position = itemsList.size -1
            val item = getItem(position)

                itemsList.remove(item)
                notifyItemRemoved(position)
        }

        fun addPage(homeItemItem:HomeItem){
            itemsList.add(homeItemItem)
            notifyItemInserted(itemsList.size-1)
        }

        fun getItem(position: Int):HomeItem{
            return itemsList.get(position)
        }

        override fun getItemViewType(position: Int): Int {
            if(position==itemsList.size-1 && isLoadingAdded){
                return LOADING
            }else return ITEM
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM) {
               val itemViewholder :ItemViewHolder = holder as ItemViewHolder
                itemsList[position].let {
                    Glide.with(holder.itemView.context)
                        .load(it.posts_image)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(itemViewholder.photo)
                }
                itemViewholder.itemView.setOnClickListener{
                    homeInterListner.itemClick(itemsList[position].id)
                }
            }else{
                val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
            }
        }

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_camera_i) as ImageView
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }

    }

}
