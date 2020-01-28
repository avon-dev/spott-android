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
        recycler_home_f.adapter = HomeAdapter(context!!, homeInterListener)

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

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface homeInter{
        fun itemClick(id: Int)
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
