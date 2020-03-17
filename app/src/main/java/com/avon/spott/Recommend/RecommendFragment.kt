package com.avon.spott.Recommend

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
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.toolbar.view.*

class RecommendFragment : Fragment(), RecommendContract.View{

    private val TAG = "forRecommendFragment"

    private lateinit var recommendPresenter: RecommendPresenter
    override lateinit var presenter: RecommendContract.Presenter

    private lateinit var recommendAdapter: RecommendAdapter
    private lateinit var layoutManager : StaggeredGridLayoutManager

    private var checkInit = false

    val recommendInterListener = object :recommendInter{
        override fun itemClick(id:Int){
            presenter.openPhoto(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //StaggeredGridLayout 아이템 재배치 시킴
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

        recommendAdapter = RecommendAdapter(context!!, recommendInterListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_user, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()
    }

    fun init(){
        recommendPresenter = RecommendPresenter(this)

        recycler_grid_user_f.layoutManager = layoutManager
        recycler_grid_user_f.adapter = recommendAdapter

        swiperefresh_user_f.setOnRefreshListener{ // 스크롤 위로 올려서 리프레시 했을 때
            Handler().postDelayed({
                logd(TAG, "refreshed!!!")

                presenter.getPhotos(getString(R.string.baseurl))
            }, 600) //로딩 주기
        }

        if(!checkInit){
            presenter.getPhotos(getString(R.string.baseurl))
        }

        //스와이퍼리프레쉬 레이아웃 색상변경
        swiperefresh_user_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

    }



    override fun onStart() {
        super.onStart()


        //툴바 처리 (뒤로가기 + 타이틀)
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE,View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE

//        MainActivity.mToolbar.text_title_toolbar.text = "Phopo 추천 장소"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_grid_user_f.layoutManager = null
    }

    interface recommendInter{
        fun itemClick(id:Int)
    }


    override fun showPhotoUi(id:Int) { //PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_recommendFragment_to_photo, bundle)
    }

    override fun addItems(homeItems: ArrayList<HomeItem>){
        recommendAdapter.addItemsAdapter(homeItems)
        recommendAdapter.notifyDataSetChanged()
        checkInit = true
    }

    override fun clearAdapter(){
        if(swiperefresh_user_f.isRefreshing){
            recommendAdapter.clearItemsAdapter()
            recommendAdapter.notifyDataSetChanged()
            swiperefresh_user_f.isRefreshing = false
        }
    }


    inner class RecommendAdapter(val context: Context, val recommendInter: recommendInter):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val ITEM = 0
        val INFO = 1
        private var itemsList = ArrayList<HomeItem>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM){
                val view =  LayoutInflater.from(context).inflate(R.layout.item_camera, parent, false)
                return ItemViewHolder(view)
            }else{
                val view =  LayoutInflater.from(context).inflate(R.layout.item_recommend_info, parent, false)
                return InfoViewHolder(view)
            }

        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(recommendItems:ArrayList<HomeItem>){
            itemsList.add(HomeItem("",0))
            itemsList.addAll(recommendItems)
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        override fun getItemViewType(position: Int): Int {
            if(position ==0){
                return INFO
            }else return ITEM
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM){
                val itmeViewholder :ItemViewHolder = holder as ItemViewHolder

                itemsList[position].let{
                    Glide.with(holder.itemView.context)
                        .load(it.posts_image)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.photo)

                    holder.itemView.setOnClickListener {
                        recommendInter.itemClick(itemsList[position].id)
                    }
                }
            }else{
                val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                layoutParams.isFullSpan = true
            }



        }


        inner class ItemViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_camera_i) as ImageView
        }

        inner class InfoViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }
    }
}