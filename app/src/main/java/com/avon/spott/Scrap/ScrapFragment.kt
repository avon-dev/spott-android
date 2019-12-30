package com.avon.spott.Scrap

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
import kotlinx.android.synthetic.main.fragment_scrap.*
import kotlinx.android.synthetic.main.fragment_scrap.view.*

class ScrapFragment : Fragment(), ScrapContract.View, View.OnClickListener {

    private lateinit var scrapPresenter: ScrapPresenter
    override lateinit var presenter: ScrapContract.Presenter

    val scrapInterListener = object :scrapInter{
        override fun itemClick() {
            presenter.openPhoto()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_scrap, container, false)

        //------[임시]swiperefreshlayout 컨트롤 + 임시 스크랩숫자------------------
        root.text_guide_scrap_f.setOnClickListener {
            if (swiperefresh_scrap_f.isRefreshing)
                swiperefresh_scrap_f.isRefreshing = false
        }

        root.text_scraps_scrap_f.text = "10"
        //-----------------------------------------------------

        return root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //---------------리사이클러뷰테스트 코드------------------------------
        val layoutManager = GridLayoutManager(context!!, 2)

        recycler_scrap_f.layoutManager = layoutManager
        recycler_scrap_f.adapter = ScrapAdapter(context!!, scrapInterListener)
        //-----------------------------------------------------------------

    }


    override fun onStart() {
        super.onStart()
        //툴바 안보이게
        mToolbar.visibility = View.GONE
    }

    fun init(){
        scrapPresenter = ScrapPresenter(this)
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_scrapFragment_to_photo)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    interface scrapInter{
        fun itemClick()
    }


    inner class ScrapAdapter(val context: Context, val scrapInterListener:scrapInter) : RecyclerView.Adapter<ScrapAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_photo_square, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(holder:ScrapFragment.ScrapAdapter.ViewHolder, position: Int) {

            //------------임시 데이터들---------------------------------------------------------------
            if (position == 0 || position == 5) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 1 || position == 6) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/06/23/17/41/morocco-2435391_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)

            } else if (position == 2 || position == 7) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 3 || position == 8) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 4 || position == 9) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }
            //---------------------------------------------------------------------------------------------------

            holder.itemView.setOnClickListener {
                scrapInterListener.itemClick()
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i) as ImageView

        }

    }

}