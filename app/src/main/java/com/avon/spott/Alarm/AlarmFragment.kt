package com.avon.spott.Alarm


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import kotlinx.android.synthetic.main.fragment_alarm.*
import kotlinx.android.synthetic.main.toolbar.view.*


class AlarmFragment : Fragment(), AlarmContract.View, View.OnClickListener {

    private lateinit var alarmPresenter: AlarmPresenter
    override lateinit var presenter: AlarmContract.Presenter

    val alarmInterListner = object : alarmInter{
        override fun photoItemClick(){
            presenter.openPhoto()
        }

        override fun commentItemClick(){
            presenter.openCommnet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val root = inflater.inflate(R.layout.fragment_alarm, container, false)

//        root.btn_photo_alarm_f.setOnClickListener {
//            findNavController().navigate(R.id.action_alarmFragment_to_photo)
//        }
//
//        root.btn_comment_alarm_f.setOnClickListener {
//
//            //photoFragment를 스택에서 날려버림
//            val navOptions = NavOptions.Builder()
//                .setPopUpTo(R.id.photoFragment, true)
//                .build()
//
//                findNavController().navigate(R.id.action_alarmFragment_to_photo)
//                findNavController().navigate(R.id.action_photoFragment_to_commentFragment,null, navOptions)
//        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //---------리사이클러뷰 테스트 코드--------------------------------
        val layoutManager = LinearLayoutManager(context!!)

        recycler_alarm_f.layoutManager  = layoutManager
        recycler_alarm_f.adapter = AlarmAdapter(context!!, alarmInterListner)
        //------------------------------------------------------------
    }


    override fun onStart() {
        super.onStart()

        // 툴바 뒤로가기, 타이틀 보이게
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.alarm)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    fun init(){
        alarmPresenter = AlarmPresenter(this)
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_alarmFragment_to_photo)
    }

    override fun showCommentUi() {
       //photoFragment를 스택에서 날려버림
         val navOptions = NavOptions.Builder()
                 .setPopUpTo(R.id.photoFragment, true)
                .build()

         findNavController().navigate(R.id.action_alarmFragment_to_photo)
         findNavController().navigate(R.id.action_photoFragment_to_commentFragment,null, navOptions)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface alarmInter{
        fun photoItemClick()
        fun commentItemClick()
    }

    inner class AlarmAdapter(
        val context: Context, val alarmInterListener:alarmInter
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val Sharing = 0
        private val Failure = 1
        private val Commnet = 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if (viewType == Sharing) {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_sharing_alarm, parent, false)
                return SharingViewHolder(view)
            } else if (viewType == Failure) {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_failure_alarm, parent, false)
                return FailureViewHolder(view)
            } else {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.item_comment_alarm, parent, false)
                return CommentViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==Sharing){
                var holder : SharingViewHolder = holder as SharingViewHolder
                holder.itemView.setOnClickListener{
                    alarmInterListener.photoItemClick()
                }
            }else if(getItemViewType(position)==Failure){
                var holder : FailureViewHolder = holder as FailureViewHolder

                holder.failreason.text = "(사유 : 잘못된 위치정보 )"

                holder.itemView.setOnClickListener{
                    alarmInterListener.photoItemClick()
                }
            }else{
                var holder : CommentViewHolder = holder as CommentViewHolder

                holder.comment.text = "회원님의 사진에 댓글을 남겼습니다"
                holder.itemView.setOnClickListener{
                    alarmInterListener.commentItemClick()
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if(position == 0 ){
                return Sharing
            }else if(position == 1){
                return  Failure
            }else return Commnet

        }


        inner class SharingViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        }

        inner class FailureViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val failreason = itemView!!.findViewById<TextView>(R.id.text_failreason_alarm_i) as TextView
        }

        inner class CommentViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val comment = itemView!!.findViewById<TextView>(R.id.text_comment_alarm_i) as TextView
        }

    }


}
