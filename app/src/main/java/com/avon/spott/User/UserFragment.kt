package com.avon.spott.User

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_mypage.*
import kotlinx.android.synthetic.main.fragment_mypage.view.*
import kotlinx.android.synthetic.main.toolbar.view.*

class UserFragment : Fragment(), UserContract.View, View.OnClickListener {


  var Userselectgrid = true


    private lateinit var userPresenter: UserPresenter
    override lateinit var presenter: UserContract.Presenter

    val userInterListener = object : userInter{
        override fun itemClick(){
            presenter.openPhoto()
        }
    }

    @SuppressLint("RestrictedApi") //floatbutton 숨기려면 해야함.
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root =  inflater.inflate(R.layout.fragment_mypage, container, false)

        root.floatimgbtn_addphoto_mypage.visibility = View.GONE

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()

        //---------------리사이클러뷰테스트 코드------------------------------
        val layoutManager = GridLayoutManager(context!!, 3)

        recycler_grid_mypage_f.layoutManager  = layoutManager
        recycler_grid_mypage_f.adapter = UserAdapter(context!!, userInterListener)
        //-----------------------------------------------------------------



        ////////마이페이지 뷰 선택 --- 일단 나중에
        val topButtonsListner = View.OnClickListener {
            if(it.id == R.id.imgbtn_grid_mypage_f) {
                imgbtn_grid_mypage_f.isSelected= true
                imgbtn_map_mypage_f.isSelected = false
                const_grid_mypage_f.visibility = View.VISIBLE
                const_map_mypage_f.visibility = View.GONE
                Userselectgrid = true
            }else{
                imgbtn_grid_mypage_f.isSelected=false
                imgbtn_map_mypage_f.isSelected = true
                const_grid_mypage_f.visibility = View.GONE
                const_map_mypage_f.visibility = View.VISIBLE
                Userselectgrid = false
            }
        }

        imgbtn_grid_mypage_f.setOnClickListener(topButtonsListner)
        imgbtn_map_mypage_f.setOnClickListener(topButtonsListner)

        if(Userselectgrid){
            imgbtn_grid_mypage_f.performClick()
        }else{
            imgbtn_map_mypage_f.performClick()
        }

        ////////////////////////////////////////////////////////

    }

    override fun onStart() {
        super.onStart()
        //-----임시 데이터-----------------------------
        Glide.with(this)
            .load(R.mipmap.ic_launcher)
            .into(MainActivity.mToolbar.img_profile_toolbar)

        MainActivity.mToolbar.text_name_toolbar.text="MyNickName"
        //--------------------------------------------
        // 툴바 유저이미지, 유저닉네임, 알람, 메뉴 보이게
        controlToobar(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE, View.VISIBLE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    fun init(){
        userPresenter = UserPresenter(this)
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_userFragment_to_photoFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    interface userInter{
        fun itemClick()
    }

    inner class UserAdapter(val context: Context, val userInterListener:userInter):RecyclerView.Adapter<UserAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 6
        }

        override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {

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
            }
            //---------------------------------------------------------------------------------------------------

            holder.itemView.setOnClickListener {
                userInterListener.itemClick()
            }
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView
        }

    }



}