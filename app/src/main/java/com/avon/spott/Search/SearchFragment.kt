package com.avon.spott.Search

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.SearchItem
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.toolbar.view.*

class SearchFragment: Fragment(), SearchContract.View, View.OnClickListener {

    private val TAG = "forSearchFragment"

    companion object{
        var recentChange = false
    }

    private lateinit var searchPresenter: SearchPresenter
    override lateinit var presenter: SearchContract.Presenter

    //검색 결과 recyclerview
    private lateinit var resultAdapter: ResultAdapter
    private lateinit var resultLayoutManager: LinearLayoutManager

    //최근 검색 recyclerview
    private lateinit var recentAdapter: RecentAdapter
    private lateinit var recentLayoutManager: LinearLayoutManager

    lateinit var watcher : TextWatcher

    private var showSearching = false

    private var checkInit = false

    private lateinit var  baseUrl :String

    val searchInterListener = object : searchInter{
        override fun hashItemClick(hash: String) {
            presenter.openHashtag(hash)
        }
        override fun recentDeleteClick(recentId:Int, position: Int) {
            presenter.deleteRecent(baseUrl, recentId, position)
        }
        override fun userItemClick(userId: Int) {
            presenter.openUser(userId)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //검색 결과 recyclerview
        resultLayoutManager = LinearLayoutManager(context!!)
        resultAdapter = ResultAdapter(context!!, searchInterListener)

        //최근 검색 recyclerview
        recentLayoutManager = LinearLayoutManager(context!!)
        recentAdapter = RecentAdapter(context!!, searchInterListener)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        baseUrl = getString(R.string.baseurl)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        const_result_search_f.visibility =if(showSearching) View.VISIBLE else View.GONE
        scroll_recent_search_f.visibility = if(showSearching) View.GONE else View.VISIBLE

        init()

        recycler_result_search_f.layoutManager = resultLayoutManager
        recycler_result_search_f.adapter = resultAdapter

        recycler_recent_search_f.layoutManager = recentLayoutManager
        recycler_recent_search_f.adapter = recentAdapter

        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        recycler_result_search_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        })

        recycler_recent_search_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                imm.hideSoftInputFromWindow(view!!.windowToken, 0)
            }
        })

    }

    override fun onStart() {
        super.onStart()

        //툴바 처리 (뒤로가기 + 검색edit)
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE)
        MainActivity.mToolbar.visibility = View.VISIBLE

        watcher =
            mToolbar.edit_search_toolbar.addTextChangedListener {
                if(it!!.trim().length>0){
                    mToolbar.edit_search_toolbar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_search_black_24dp, 0, R.drawable.ic_close_grey_20dp,0)
                    scroll_recent_search_f.visibility = View. GONE
                    const_result_search_f.visibility = View.VISIBLE
                    showSearching = true

                    presenter.getSearching(baseUrl, it.toString())

                }else{
                    mToolbar.edit_search_toolbar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_search_black_24dp, 0, 0,0)

                    scroll_recent_search_f.visibility = View.VISIBLE
                    const_result_search_f.visibility = View.GONE
                    showSearching = false
                }
            }

//        if(recentChange){
//            recentChange = false
//            여기서부터 다시해야함~!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        }

    }

    override fun onStop() {
        super.onStop()
        mToolbar.edit_search_toolbar.removeTextChangedListener(watcher)


        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        recycler_result_search_f.layoutManager = null
        recycler_recent_search_f.layoutManager = null
    }

    override fun onDestroy() {
        super.onDestroy()

        mToolbar.edit_search_toolbar.setText("")
    }

    fun init(){
        searchPresenter = SearchPresenter(this)


        mToolbar.edit_search_toolbar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val DRAWABLE_RIGHT = 2

                when(event?.action){
                    MotionEvent.ACTION_UP ->{
                        if(mToolbar.edit_search_toolbar.compoundDrawables[DRAWABLE_RIGHT]!=null){
                            logd(TAG, "event.rawX :"+ event.rawX)
                            logd(TAG, "right : "+ mToolbar.edit_search_toolbar.right)
                            logd(TAG, "width : "+mToolbar.edit_search_toolbar.compoundDrawables[DRAWABLE_RIGHT].bounds.width())
                            if(event.rawX >= (mToolbar.edit_search_toolbar.right - mToolbar.edit_search_toolbar.compoundDrawables[DRAWABLE_RIGHT].bounds.width()-30)){
                                mToolbar.edit_search_toolbar.setText("")
                                return v?.onTouchEvent(event) ?: true
                            }
                        }

                    }
                }
                return v?.onTouchEvent(event) ?: false
            }
        })

        if(!checkInit){
           presenter.getRecent(baseUrl)
            checkInit = true
        }

        text_deleteall_search_f.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.text_deleteall_search_f ->{
                presenter.deleteAll(baseUrl)
            }
        }
    }

    override fun showUserUi(userId: Int) {
        val bundle = Bundle()
        bundle.putInt("SearcheduserId", userId)
        findNavController().navigate(R.id.action_searchFragment_to_searchedUserFragment, bundle)
    }

    override fun showHashtag(hashtag: String) {
        val bundle = Bundle()
        bundle.putString("Searchedhashtag", hashtag)
        findNavController().navigate(R.id.action_searchFragment_to_searchedHashtagFragment,bundle)
    }

    override fun addResultItems(resultItems:ArrayList<SearchItem>){
        resultAdapter.clearItemsAdapter()
        resultAdapter.addItemsAdapter(resultItems)
        resultAdapter.notifyDataSetChanged()
    }

    override fun addRecentItems(recentItems:ArrayList<SearchItem>){
        recentAdapter.clearItemsAdapter()
        recentAdapter.addItemsAdapter(recentItems)
        recentAdapter.notifyDataSetChanged()

        //최근 검색어 없을시
        if(recentItems.size>0) text_norecent_search_f.visibility = View.GONE
        else text_norecent_search_f.visibility = View.VISIBLE
    }

    override fun clearRecentItems(){
        recentAdapter.clearItemsAdapter()
        recentAdapter.notifyDataSetChanged()
        text_norecent_search_f.visibility = View.VISIBLE
    }

    override fun deleteRecentItem(position: Int){
        recentAdapter.deleteItemsAdapter(position)
        recentAdapter.notifyDataSetChanged()
        if(recentAdapter.itemsList.size == 0){
            text_norecent_search_f.visibility = View.VISIBLE
        }
    }

    interface searchInter{
        fun hashItemClick(hash:String)
        fun userItemClick(userId:Int)
        fun recentDeleteClick(recentId:Int, position: Int)
    }

    inner class ResultAdapter(val context: Context, val searchInterListnener:searchInter):RecyclerView.Adapter<ResultAdapter.ViewHolder>(){

        private var itemsList = ArrayList<SearchItem>()

        override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ResultAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(searchItems:ArrayList<SearchItem>){
            itemsList.addAll(searchItems)
        }

        fun clearItemsAdapter(){
            itemsList.clear()
        }

        override fun onBindViewHolder(holder: ResultAdapter.ViewHolder, position: Int) {
            itemsList[position].let{
                if(itemsList[position].is_tag){
                    holder.userPhoto.visibility = View.GONE
                    holder.bigHash.visibility = View.VISIBLE
                    holder.resultText.text = it.name

                    holder.itemView.setOnClickListener{
                        searchInterListnener.hashItemClick(itemsList[position].name!!)
                    }

                }else{
                    holder.userPhoto.visibility = View.VISIBLE
                    holder.bigHash.visibility = View.GONE
                    holder.resultText.text = it.nickname

                    if(it.profile_image  ==null){
                        holder.userPhoto.setImageResource(R.drawable.img_person)
                    }else{
                        Glide.with(holder.itemView.context)
                            .load(it.profile_image)
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .into(holder.userPhoto)
                    }


                    holder.itemView.setOnClickListener{
                        searchInterListnener.userItemClick(itemsList[position].id!!)
                    }

                }
            }

        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val userPhoto =  itemView.findViewById<ImageView>(R.id.img_result_search_i)
            val resultText = itemView.findViewById<TextView>(R.id.text_result_search_i)
            val bigHash = itemView.findViewById<TextView>(R.id.text_hash_result_search_i)
        }


    }

    inner class RecentAdapter(val context: Context, val searchInterListnener: searchInter):RecyclerView.Adapter<RecentAdapter.ViewHolder>(){
        var itemsList = ArrayList<SearchItem>()

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecentAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(searchItems:ArrayList<SearchItem>){
            itemsList.addAll(searchItems)
        }

        fun clearItemsAdapter(){
            itemsList.clear()
        }

        fun deleteItemsAdapter(position: Int){
            itemsList.removeAt(position)
        }

        override fun onBindViewHolder(holder: RecentAdapter.ViewHolder, position: Int){
            holder.close.visibility = View.VISIBLE

            itemsList[position].let{
                if(itemsList[position].is_tag){
                    holder.userPhoto.visibility = View.GONE
                    holder.bigHash.visibility = View.VISIBLE
                    holder.resultText.text = it.name

                    holder.itemView.setOnClickListener{
                        searchInterListnener.hashItemClick(itemsList[position].name!!)
                    }

                }else{
                    holder.userPhoto.visibility = View.VISIBLE
                    holder.bigHash.visibility = View.GONE
                    holder.resultText.text = it.nickname

                    if(it.profile_image  ==null){
                        holder.userPhoto.setImageResource(R.drawable.img_person)
                    }else{
                        Glide.with(holder.itemView.context)
                            .load(it.profile_image)
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .into(holder.userPhoto)
                    }


                    holder.itemView.setOnClickListener{
                        searchInterListnener.userItemClick(itemsList[position].id!!)
                    }
                }

                holder.close.setOnClickListener {
                    // 최근 검색어 삭제 눌렀을 때
                    searchInterListnener.recentDeleteClick(itemsList[position].recentId!!, position)
                }


            }

        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val userPhoto =  itemView.findViewById<ImageView>(R.id.img_result_search_i)
            val resultText = itemView.findViewById<TextView>(R.id.text_result_search_i)
            val bigHash = itemView.findViewById<TextView>(R.id.text_hash_result_search_i)
            val close = itemView.findViewById<ImageButton>(R.id.imgbtn_delete_recent_search_i)
        }
    }

    override fun showDeleteAll(show:Boolean){
        text_deleteall_search_f.visibility = if(show) View.VISIBLE else View.GONE
    }
}