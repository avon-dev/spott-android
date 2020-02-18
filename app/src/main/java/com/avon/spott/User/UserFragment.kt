package com.avon.spott.User

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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.MapCluster
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToolbar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.toolbar.view.*

class UserFragment : Fragment(), UserContract.View, View.OnClickListener{

    private val TAG = "forUserFragment"

    lateinit var mapRecyclerViewUser : RecyclerView  //Map recyclerview

    private lateinit var userPresenter: UserPresenter
    override lateinit var presenter: UserContract.Presenter

    //Grid recyclerview
    private lateinit var userAdapter: UserAdapter
    private lateinit var layoutManager : GridLayoutManager

    //서버에서 불러온 내 전체 아이템들
    private var wholeItems:ArrayList<MapCluster>? = null

    //유저의 닉네임과 아이디
    private var userNickname:String? = null
    private var userPhoto:String? = null

    private var checkInit = false

    val userInterListener = object : userInter{
        override fun itemClick(id:Int){
            presenter.openPhoto(id)
        }
    }

    private var fromSearch = false
    private var userId = 0

    private var isPublic = true
    private var myself = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Grid recyclerview 용
        layoutManager = GridLayoutManager(context!!, 3)
        userAdapter = UserAdapter(context!!, userInterListener)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_user, container, false)

        logd(TAG, "user's id : "+arguments?.getInt("userId"))
        logd(TAG, "from search user' id : "+arguments?.getInt("SearcheduserId"))

        if(arguments?.getInt("userId")!=0){
            userId = arguments?.getInt("userId")!!
        }else{
            userId = arguments?.getInt("SearcheduserId")!!
            fromSearch = true
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()


        if(userNickname!=null){
            setUserInfo(userNickname!!, userPhoto, isPublic, myself)
        }

        recycler_grid_user_f.layoutManager  = layoutManager
        recycler_grid_user_f.adapter = userAdapter


        swiperefresh_user_f.setOnRefreshListener {
            Handler().postDelayed({
                presenter.getUserphotos(getString(R.string.baseurl), userId, false)
            }, 600) //로딩 주기
        }

        if(!checkInit) {
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
            presenter.getUserphotos(getString(R.string.baseurl), userId, fromSearch)

            checkInit = true
        }

        swiperefresh_user_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

    }

    override fun onStart() {
        super.onStart()

        // 툴바 유저이미지, 유저닉네임 보이게
        controlToolbar(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE

        if( wholeItems!=null &&  wholeItems!!.size == 0){ //서버에서 불러왔던 사진아이템 사이즈가 0이면 사진없음 문구 보이게
            text_nophoto_user_f.visibility = View.VISIBLE
        }



    }



    override fun onDestroyView() {
        super.onDestroyView()

        recycler_grid_user_f.layoutManager = null

    }

    fun init(){
        userPresenter = UserPresenter(this)
    }

    override fun showPhotoUi(id:Int) {//PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        if(!fromSearch){
            findNavController().navigate(R.id.action_userFragment_to_photoFragment, bundle)
        }else{
            findNavController().navigate(R.id.action_searchedUserFragment_to_photo, bundle)
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    override fun clearAdapter(){
        if(swiperefresh_user_f.isRefreshing){
            userAdapter.clearItemsAdapter()
            userAdapter.notifyDataSetChanged()
            swiperefresh_user_f.isRefreshing = false
        }
    }


    override fun addItems(userItems:ArrayList<MapCluster>){
        logd(TAG, "addItems : " + userItems)
        wholeItems = userItems

        userAdapter.addItemsAdapter(userItems)
        userAdapter.notifyDataSetChanged()
    }


    override fun noPhoto(){
        text_nophoto_user_f.visibility = View.VISIBLE
    }


    interface userInter{
        fun itemClick(id:Int)
    }

    inner class UserAdapter(val context: Context, val userInterListener:userInter):RecyclerView.Adapter<UserAdapter.ViewHolder>(){

        private var itemsList = ArrayList<MapCluster>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
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

        override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {

           itemsList[position].let{
               Glide.with(holder.itemView.context)
                   .load(it.posts_image)
                   .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                   .error(android.R.drawable.stat_notify_error)
                   .into(holder.photo)

               holder.itemView.setOnClickListener {
                   userInterListener.itemClick(itemsList[position].id)
               }
           }


        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

    }


    override fun setUserInfo(nickname:String, photo:String?, isPublic: Boolean, myself:Boolean){
        userNickname = nickname
        userPhoto = photo
        this.myself = myself

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

    private fun showPublic(isPublic: Boolean) {
        this.isPublic = isPublic
        if(!myself){
            const_private_user_f.visibility = if(isPublic) View.GONE else View.VISIBLE
        }else const_private_user_f.visibility = View.GONE

    }



}