package com.avon.spott.Hashtag

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.HomeItem
import com.avon.spott.Home.HomeFragment
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.toolbar.view.*

class HashtagFragment: Fragment(), HashtagContract.View, View.OnClickListener{

    private val TAG = "forHashtagFragment"

    private lateinit var hashPresenter :HashtagPresenter
    override lateinit var presenter: HashtagContract.Presenter

    private lateinit var hashtagAdapter : HashtagAdapter
    private lateinit var layoutManager : GridLayoutManager

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 30  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함
    override var hasNext: Boolean = false // 다음으로 가져올 페이지가 있는지 여부
    override var refreshTimeStamp:String = "" // 서버에서 페이지를 가지고오는 시간적 기준점(페이징 도중 사진이 추가될 때 중복됨을 방지)

    private var checkInit = false

    private val PHOTO = 1101
    private val SEARCH = 1102
    private val NOTI = 1103

    private var comeFrom = 0
    private var fromSearch = false
    private var hashtag = ""

    private var nohashtag = false

    val hashtagInterListener = object :hashtagInter{
        override fun itemClick(id:Int){
            presenter.openPhoto(id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = GridLayoutManager(context!!, 3)

        layoutManager.setSpanSizeLookup(object :GridLayoutManager.SpanSizeLookup(){
            override fun getSpanSize(position: Int): Int {
                if(hashtagAdapter.getItemViewType(position) == 0){
                    return 1
                }else{
                    return 3
                }
            }
        })

        hashtagAdapter = HashtagAdapter(context!!, hashtagInterListener)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_user, container, false)

        if(arguments?.getString("hashtag")!= null){
            hashtag = arguments?.getString("hashtag")!!
            comeFrom = PHOTO
        }else if(arguments?.getString("notiHashtag")!= null) {
            hashtag = arguments?.getString("notiHashtag")!!
            comeFrom = NOTI
        }else{
            hashtag = arguments?.getString("Searchedhashtag")!!
            comeFrom = SEARCH
            fromSearch = true
        }

//        showToast("from Seached : " + fromSearch + " & hashtag : "+hashtag)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

        recycler_grid_user_f.layoutManager = layoutManager
        recycler_grid_user_f.adapter = hashtagAdapter

        recycler_grid_user_f.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(!recycler_grid_user_f.canScrollVertically(1)) { // 맨 아래에서 스크롤 할 때
                    if(!pageLoading && hasNext) { //이미 페이징을 불러오는 동안 중복요청하지 않게하기위함. + 다음 가져올 페이지가 있는지 여부확인
                        logd("ScrollTEST", "END")
                        pageLoading = true

                        hashtagAdapter.addPageLoadingItem() //리싸이클러뷰에 로딩아이템 생성 생성
                        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount-1)

                        Handler().postDelayed({
                            presenter.getPhotos(getString(R.string.baseurl), start, hashtag, false)
                        }, 400) //로딩 주기
                    }
                }
            }
        })

        swiperefresh_user_f.setOnRefreshListener {
            Handler().postDelayed({

                //페이징 시작위치와 시간 초기화
                start = 0
                refreshTimeStamp = ""
                presenter.getPhotos(getString(R.string.baseurl), start, hashtag, false)

            }, 600) //로딩 주기
        }

        if(!checkInit){
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
            presenter.getPhotos(getString(R.string.baseurl), start, hashtag, fromSearch)
        }

        swiperefresh_user_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

    }

    override fun onStart() {
        super.onStart()

        //툴바 처리 (뒤로가기 + 타이틀)
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE

        mToolbar.text_title_toolbar.text = "#"+hashtag

        showNohashtag(nohashtag) //해시태그 없을 땐 없다는 문구 보여주기

    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_grid_user_f.layoutManager = null
    }

    fun init(){
        hashPresenter = HashtagPresenter(this)
    }

    override fun addItems(homeItemItems: ArrayList<HomeItem>){
        start = start + pageItems

        hashtagAdapter.addItemsAdapter(homeItemItems)
        hashtagAdapter.notifyDataSetChanged()

        pageLoading = false

        checkInit = true
    }

    override fun removePageLoading(){
        hashtagAdapter.removePageLoadingItem()
    }

    override fun clearAdapter(){
        if(swiperefresh_user_f.isRefreshing){
            hashtagAdapter.clearItemsAdapter()
            hashtagAdapter.notifyDataSetChanged()
            swiperefresh_user_f.isRefreshing = false
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun showPhotoUi(photoId:Int){
        val bundle = bundleOf("photoId" to photoId)
        if(comeFrom == PHOTO){
            findNavController().navigate(R.id.action_hashtagFragment_to_photoFragment, bundle)
        }else if(comeFrom == NOTI){
            findNavController().navigate(R.id.action_notiHashtagFragment_to_photo, bundle)
        }else{
            findNavController().navigate(R.id.action_searchedHashtagFragment_to_photo, bundle)
        }
    }

    interface hashtagInter{
        fun itemClick(id:Int)
    }

    inner class HashtagAdapter(val context: Context, val hashtagInterListener:hashtagInter):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private var itemsList = ArrayList<HomeItem>()

        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM) { //아이템일 때 아이템뷰홀더 선택
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
            if (getItemViewType(position) == ITEM) {
                val itemViewholder: HashtagAdapter.ItemViewHolder =
                    holder as HashtagAdapter.ItemViewHolder
                itemsList[position].let {
                    Glide.with(holder.itemView.context)
                        .load(it.posts_image)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(itemViewholder.photo)
                }
                itemViewholder.itemView.setOnClickListener {
                    hashtagInterListener.itemClick(itemsList[position].id)
                }
            }
        }


        inner class ItemViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

        }
    }

    override fun showNohashtag(boolean: Boolean){
        if(boolean){
            text_nohashtag_user_f.text = "#"+hashtag+" "+getString(R.string.text_no_hashtag)
            text_nohashtag_user_f.visibility = View.VISIBLE
            nohashtag = true
        }else{
            text_nohashtag_user_f.visibility = View.GONE
            nohashtag = false
        }
    }
}