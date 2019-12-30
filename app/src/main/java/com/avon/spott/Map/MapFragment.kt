package com.avon.spott.Map


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
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_list.*

class MapFragment : Fragment() , MapContract.View, View.OnClickListener {


    private lateinit var mapPresenter: MapPresenter
    override lateinit var presenter: MapContract.Presenter

    lateinit var childfragment :Fragment

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
            println("이 지역에서 찾기!!!!!!!!!!!!!!!!!!")
        }
        //-----------------------------------------------

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
                    if( bsb.state == BottomSheetBehavior.STATE_COLLAPSED){
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



}









