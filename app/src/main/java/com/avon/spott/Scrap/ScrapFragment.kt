package com.avon.spott.Scrap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Camera.CameraXActivity
import com.avon.spott.Data.ScrapItem
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_scrap.*

class ScrapFragment : Fragment(), ScrapContract.View, View.OnClickListener {

    private val TAG = "forScrapFragment"

    companion object{
        var scrapChange = false
    }

    private lateinit var scrapPresenter: ScrapPresenter
    override lateinit var presenter: ScrapContract.Presenter

    private lateinit var scrapAdapter: ScrapAdapter
    private lateinit var layoutManager : GridLayoutManager

    private var checkInit = false

    private var scrapCount = 0

    val scrapInterListener = object :scrapInter{
        override fun itemClick(id: Int) {
            presenter.openPhoto(id)
        }

        override fun itemLongClick(postPhotoUrl:String, backPhotoUrl: String?) {
            presenter.openCamera(postPhotoUrl, backPhotoUrl)
        }

        override fun deleteScraps(scrapItems: ArrayList<ScrapItem>){
            presenter.deleteScraps(getString(R.string.baseurl), scrapItems)
        }

        override fun returnText() {
            showReady(false)
        }

        override fun counttext(count: Int) {
            newCount(count)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = GridLayoutManager(context!!, 2)

        scrapAdapter = ScrapAdapter(context!!, scrapInterListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_scrap, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        recycler_scrap_f.layoutManager = layoutManager
        recycler_scrap_f.adapter = scrapAdapter


        swiperefresh_scrap_f.setOnRefreshListener {
            Handler().postDelayed({
                presenter.getScraps(getString(R.string.baseurl))
            }, 300) //로딩 주기
        }

        swiperefresh_scrap_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))


        if(!checkInit){
            presenter.getScraps(getString(R.string.baseurl))

        }else{

            text_scraps_scrap_f.text = scrapCount.toString()

            const_noscrap_scrap_f.visibility =   if(scrapCount==0) View.VISIBLE else View.GONE
            text_deleteready_scrap_f.visibility = if(scrapCount==0) View.GONE else View.VISIBLE

            if(scrapChange){
                scrapAdapter.clearItemsAdapter()
                scrapAdapter.notifyDataSetChanged()
                presenter.getScraps(getString(R.string.baseurl))
                scrapChange = false
            }
        }

    }

    override fun onStart() {
        logd(TAG, "  onStart")
        super.onStart()
        //툴바 안보이게
        mToolbar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scrapAdapter.stopSelecting()
        recycler_scrap_f.layoutManager = null
    }

    fun init(){
        scrapPresenter = ScrapPresenter(this)

        text_delete_scrap_f.setOnClickListener(this)
        text_deleteready_scrap_f.setOnClickListener(this)
    }

    override fun showPhotoUi(id:Int) { //PhotoFragment로 이동
        val bundle = bundleOf("photoId" to id)
        findNavController().navigate(R.id.action_scrapFragment_to_photo, bundle)
    }

    override fun showCameraUi(postPhotoUrl:String, backPhotoUrl: String?) {
//        showToast(photoUrl)
        /**
         * 여기에 카메라 연결하는 코드 넣으면 됨!!!!!
         *                                    */

        Intent(context, CameraXActivity::class.java).let {
            it.putExtra("postPhotoUrl", postPhotoUrl)
            it.putExtra("backPhotoUrl", backPhotoUrl)
            startActivity(it)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.text_deleteready_scrap_f ->{
                scrapAdapter.readyToSelect()

               showReady(true)
            }

            R.id.text_delete_scrap_f ->{
                scrapAdapter.deleteScraps()
            }
        }
    }

    interface scrapInter{
        fun itemClick(id: Int)
        fun itemLongClick(postPhotoUrl:String, backPhotoUrl: String?)
        fun deleteScraps(scrapItems: ArrayList<ScrapItem>)
        fun returnText()
        fun counttext(count: Int)
    }


    inner class ScrapAdapter(val context: Context, val scrapInterListener:scrapInter) : RecyclerView.Adapter<ScrapAdapter.ViewHolder>() {

        private var itemsList = ArrayList<ScrapItem>()

        private var selectingReady = false
        private var isDeleting = false
        var deletelist = ArrayList<ScrapItem>()


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScrapAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_scrap, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return itemsList.size
        }

        fun clearItemsAdapter() {
            itemsList.clear()
        }

        fun addItemsAdapter(scrapItems: ArrayList<ScrapItem>){
            itemsList.addAll(scrapItems)
        }

        fun readyToSelect(){
            selectingReady = true
            notifyDataSetChanged()
        }

        fun stopSelecting(){
            selectingReady = false
            deletelist.clear()
            notifyDataSetChanged()
        }

        fun deleteItemsAdater(scrapItems: ArrayList<ScrapItem>){
            selectingReady = false
            itemsList.removeAll(scrapItems)
            deletelist.clear()
            isDeleting = false
            notifyDataSetChanged()
            scrapInterListener.returnText()
            scrapInterListener.counttext(itemsList.size)
        }

        fun deleteItemsError(){
            isDeleting = false
        }

        fun deleteScraps(){
            if(!isDeleting){
               if( deletelist.size>0) {
                   isDeleting = true
                   scrapInterListener.deleteScraps(deletelist)
               }else{
                   stopSelecting()
                   scrapInterListener.returnText()
               }
            }

        }


        override fun onBindViewHolder(holder:ScrapFragment.ScrapAdapter.ViewHolder, position: Int) {

            itemsList[position].let {
                Glide.with(holder.itemView.context)
                    .load(it.posts_image)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }

            holder.itemView.setOnClickListener {
                if(!selectingReady){
                    scrapInterListener.itemClick(itemsList[position].id)
                }else{
                    if(!holder.check){
                        holder.check = true
                        holder.selectBackground.setBackgroundResource(R.color.scrap_selected_background)
                        holder.selectCheck.setBackgroundResource(R.drawable.ic_check_white_36dp)
                        deletelist.add(itemsList[position])
                    }else{
                        holder.check = false
                        holder.selectBackground.setBackgroundResource(R.color.scrap_unslected_background)
                        holder.selectCheck.setBackgroundResource(R.drawable.ic_check_tpwhite_36dp)
                        deletelist.remove(itemsList[position])
                    }
                }
            }

             holder.itemView.isLongClickable = true

            holder.itemView.setOnLongClickListener(object :View.OnLongClickListener{
                override fun onLongClick(v: View?): Boolean {
                     if(!selectingReady){
                      scrapInterListener.itemLongClick(itemsList[position].posts_image, itemsList[position].back_image)
                    }
                    return true
                }
            })







            if(selectingReady){
                holder.selectBackground.visibility = View.VISIBLE
            }else{
                deletelist.clear()
                holder.selectBackground.visibility = View.GONE
                holder.check = false
                holder.selectBackground.setBackgroundResource(R.color.scrap_unslected_background)
                holder.selectCheck.setBackgroundResource(R.drawable.ic_check_tpwhite_36dp)
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_scrap_i) as ImageView

            val selectBackground = itemView.findViewById<LinearLayout>(R.id.linear_background_scrap_i)
            val selectCheck = itemView.findViewById<ImageView>(R.id.img_check_scrap_i)

            var check = false

        }

    }

    override fun clearAdapter(){
        if(swiperefresh_scrap_f.isRefreshing){
            scrapAdapter.clearItemsAdapter()
            scrapAdapter.notifyDataSetChanged()
            swiperefresh_scrap_f.isRefreshing = false
        }
    }

    override fun addItems(scrapItems:ArrayList<ScrapItem>){
        scrapAdapter.addItemsAdapter(scrapItems)
        scrapAdapter.notifyDataSetChanged()

        scrapCount = scrapItems.size
        text_scraps_scrap_f.text = scrapCount.toString()
        if(scrapCount ==0){
            const_noscrap_scrap_f.visibility = View.VISIBLE
            text_deleteready_scrap_f.visibility = View.GONE
        }else{
            const_noscrap_scrap_f.visibility = View.GONE
            text_deleteready_scrap_f.visibility = View.VISIBLE
        }

        checkInit = true
    }

    override fun showReady(boolean: Boolean){
       text_deleteready_scrap_f.visibility = if(boolean) View.GONE else View.VISIBLE
        text_delete_scrap_f.visibility = if(boolean) View.VISIBLE else View.GONE
    }

    override fun deleteDone(scrapItems: ArrayList<ScrapItem>){
        scrapAdapter.deleteItemsAdater(scrapItems)
    }

    override fun deleteError(){
        scrapAdapter.deleteItemsError()
        showToast(getString(R.string.server_connection_error))
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    fun newCount(count:Int) {
        text_scraps_scrap_f.text = count.toString()
        scrapCount = count

        if(count==0){
            const_noscrap_scrap_f.visibility = View.VISIBLE
            text_deleteready_scrap_f.visibility = View.GONE
        }else{
            text_deleteready_scrap_f.visibility = View.VISIBLE
        }

    }


}