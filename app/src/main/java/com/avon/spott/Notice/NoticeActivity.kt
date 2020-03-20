package com.avon.spott.Notice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.Notice
import com.avon.spott.R
import com.avon.spott.Utils.DateTimeFormatter
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.activity_notice.*
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.toolbar.view.*

class NoticeActivity : AppCompatActivity(), NoticeContract.View, View.OnClickListener{

    private val TAG = "forNoticeActivity"

    private lateinit var noticePresenter: NoticePresenter
    override lateinit var presenter: NoticeContract.Presenter

    private lateinit var noticeAdpater: NoticeAdpater
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notice)

        //툴바 타이틀 넣기
        include_toolbar_notice_a.text_title_toolbar.text = getString(R.string.text_notice)

        init()
    }

    private fun init(){
        noticePresenter = NoticePresenter(this)
        include_toolbar_notice_a.img_back_toolbar.setOnClickListener(this)

        layoutManager = LinearLayoutManager(this)
        noticeAdpater = NoticeAdpater(this, noticeInterListener)

        recycler_notice_a.layoutManager = layoutManager
        recycler_notice_a.adapter = noticeAdpater

        swiperefresh_notice_a.setOnRefreshListener {
            Handler().postDelayed({

              presenter.getNotice(getString(R.string.baseurl))

            }, 600) //로딩 주기
        }

        swiperefresh_notice_a.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary))


        presenter.getNotice(getString(R.string.baseurl))
    }

    override fun onDestroy() {
        super.onDestroy()
        recycler_notice_a.layoutManager = null
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar ->{ presenter.navigateUp() }
        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showNoticeDetailUi(restUrl: String){
        val intent = Intent(this@NoticeActivity, NoticeDetailActivity::class.java)
        intent.putExtra("noticeRestUrl", restUrl)
        startActivity(intent)
    }

    val noticeInterListener = object : noticeInter{
        override fun itemClick(restUrl: String) {
            presenter.openNoticeDetail(restUrl)
        }
    }

    override fun addItems(noticeItems: ArrayList<Notice>){
        noticeAdpater.addItemsAdapter(noticeItems)
        noticeAdpater.notifyDataSetChanged()
    }

    override fun clearAdapter(){
        if(swiperefresh_notice_a.isRefreshing){
            noticeAdpater.clearItemsAdapter()
            noticeAdpater.notifyDataSetChanged()
            swiperefresh_notice_a.isRefreshing = false
        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface noticeInter{
        fun itemClick(restUrl:String)
    }

    inner class NoticeAdpater(
        val context: Context, val noticeInter:noticeInter
    ):RecyclerView.Adapter<NoticeAdpater.ViewHolder>(){
        private var itemsList = ArrayList<Notice>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_notice,parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun addItemsAdapter(noticeItems:ArrayList<Notice>){
            itemsList.addAll(noticeItems)
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        override fun onBindViewHolder(holder: NoticeAdpater.ViewHolder, position: Int) {
            itemsList[position].let{
                holder.title.text = it.title
                holder.date.text = DateTimeFormatter.convertLocalDate(it.created_date)

                holder.itemView.setOnClickListener {
                    noticeInter.itemClick(itemsList[position].contents_url)
                }
            }
        }

        inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
            val title = itemView.findViewById<TextView>(R.id.text_title_notice_i) as TextView
            val date = itemView.findViewById<TextView>(R.id.text_date_notice_i) as TextView
        }
    }
}