package com.avon.spott.Home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.avon.spott.Data.Home
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment(), HomeContract.View, View.OnClickListener {

    private val TAG = "forHomeFragment"

    private lateinit var homePresenter: HomePresenter
    override lateinit var presenter: HomeContract.Presenter

    private lateinit var homeAdapter: HomeAdapter

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 10  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함

    val homeInterListener = object :homeInter{
        override fun itemClick(id: Int) {
            presenter.openPhoto(id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        logd(TAG, "onCreateView")

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        logd(TAG, "onActivityCreated")

        init()

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //아이템이 재배치 되는 코드
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        recycler_home_f.layoutManager = layoutManager
        homeAdapter = HomeAdapter(context!!, homeInterListener)
        recycler_home_f.adapter = homeAdapter

        recycler_home_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(recycler_home_f.canScrollVertically(1)){
                    logd(TAG, "End of Scroll")
                    if(!pageLoading){ //이미 페이징을 불러오는 동안 중복요청하지 않게하기위함.
                        pageLoading = true
                        getPagedItems()
                    }
                }
            }
        })

        //여기에 서버에서 리스트를 불러오는 코드를 넣어야한다.

    }


    override fun onStart() {
        super.onStart()
        //툴바 안보이게
        mToolbar.visibility = View.GONE
    }

    fun init(){
        homePresenter = HomePresenter(this)
    }

    override fun showPhotoUi(id:Int) { //PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_mapFragment_to_photo, bundle)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    private fun getPagedItems() { //페이징으로 나눠서 아이템 추가
        if(start!=0){
            homeAdapter.removeLoadingItem()
        }
        pageLoading = false
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface homeInter{
        fun itemClick(id: Int)
    }

    override fun addItems(homeItems: ArrayList<Home>){
        homeAdapter.addItemsAdapter(homeItems)
        homeAdapter.notifyDataSetChanged()
    }

    override fun removeLoading(){
        homeAdapter.removeLoadingItem()
    }

    inner class HomeAdapter(val context: Context, val homeInterListner:homeInter) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private var itemsList = ArrayList<Home>()

        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM) { //아이템일 때 아이템뷰홀더 선택
                val view = LayoutInflater.from(context).inflate(R.layout.item_camera, parent, false)
                return ItemViewHolder(view)
            }else{//로딩일 때 로딩뷰홀더 선택
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

        fun addItemsAdapter(homeItems: ArrayList<Home>){
            itemsList.addAll(homeItems)
        }

        fun addPageItem(homeItem:Home){
            itemsList.add(homeItem)
        }

        fun addLoadingItem() {
            isLoadingAdded = true
            add(Home("",0))
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

        fun add(homeItem:Home){
            itemsList.add(homeItem)
            notifyItemInserted(itemsList.size-1)
        }

        fun getItem(position: Int):Home{
            return itemsList.get(position)
        }

        override fun getItemViewType(position: Int): Int {
            if(position==itemsList.size-1 && isLoadingAdded){
                return LOADING
            }else return ITEM
        }

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
                    homeInterListner.itemClick(itemsList[position].id)
                }
            }
        }

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_camera_i) as ImageView
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }

    }

}
