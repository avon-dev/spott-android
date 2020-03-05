package com.avon.spott.Notification


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.NotiItem
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToolbar
import com.avon.spott.Utils.DateTimeFormatter
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_notification.*
import kotlinx.android.synthetic.main.toolbar.view.*


class NotificationFragment : Fragment(), NotificationContract.View, View.OnClickListener {

    private val TAG = "forNotificationFragment"

    private lateinit var notiPresenter: NotificationPresenter
    override lateinit var presenter: NotificationContract.Presenter

    private lateinit var notiAdapter: NoificationAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 20  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함
    override var hasNext: Boolean = false // 다음으로 가져올 페이지가 있는지 여부
    override var refreshTimeStamp:String = "" // 서버에서 페이지를 가지고오는 시간적 기준점(페이징 도중 사진이 추가될 때 중복됨을 방지)

    private var checkInit = false

    val notiInterListener = object : notiInter{
        override fun photoClick(photoId:Int){
            presenter.openPhoto(photoId)
        }

        override fun commentClick(photoId:Int){
            presenter.openComment(photoId)
        }

        override fun userPhotoClick(userId:Int) {
            presenter.openUser(userId)
        }

        override fun reasonClick(notiId:Int,kind:Int, reason:String) {
            presenter.openReason(notiId, kind, reason)
        }

        override fun deleteNoti(position: Int, notiId:Int){
            presenter.deleteNoti(getString(R.string.baseurl), notiId, position)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutManager = LinearLayoutManager(context!!)
        notiAdapter = NoificationAdapter(context!!, notiInterListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val root = inflater.inflate(R.layout.fragment_notification, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

    }

    override fun onStart() {
        super.onStart()

        // 툴바 뒤로가기, 타이틀 보이게
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.notification)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_noti_f.layoutManager = null
    }

    fun init(){
        notiPresenter = NotificationPresenter(this)

        recycler_noti_f.layoutManager = layoutManager
        recycler_noti_f.adapter = notiAdapter
        recycler_noti_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recycler_noti_f.canScrollVertically(1)) { // 맨 아래에서 스크롤 할 때
                    if(!pageLoading && hasNext) { //이미 페이징을 불러오는 동안 중복요청하지 않게하기위함. + 다음 가져올 페이지가 있는지 여부확인
                        pageLoading = true
                        notiAdapter.addPageLoadingItem()
                        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount-1)
                        Handler().postDelayed({
                              presenter.getNoti(getString(R.string.baseurl), start)
                        }, 400) //로딩 주기
                    }
                }
            }
        })

        swiperefresh_noti_f.setOnRefreshListener {
            Handler().postDelayed({
                //페이징 시작위치와 시간 초기화
                start = 0
                refreshTimeStamp = ""

                 presenter.getNoti(getString(R.string.baseurl), start)
            }, 600) //로딩 주기
        }

        if(!checkInit) {
            logd(TAG, "getnoti")
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
                 presenter.getNoti(getString(R.string.baseurl), start)

        }

        //스와이퍼리프레쉬 레이아웃 색상변경
        swiperefresh_noti_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))

    }

    override fun showPhotoUi(photoId:Int) {
        val bundle = bundleOf("photoId" to photoId)
        findNavController().navigate(R.id.action_notiFragment_to_photo, bundle)
    }

    override fun showCommentUi(photoId:Int) {

         val bundle = bundleOf("notiPhotoId" to photoId)
         findNavController().navigate(R.id.action_notiFragment_to_notiCommentFragment, bundle)

    }

    override fun showReasonUi(notiId: Int, kind:Int, reason:String) {
        val bundle = Bundle()
        bundle.putInt("notiId", notiId)
        bundle.putInt("kind", kind)
        bundle.putString("reason", reason)
        findNavController().navigate(R.id.action_notiFragment_to_reasonFragment, bundle)
    }

    override fun showUserUi(userId: Int) {
        val bundle = bundleOf("notiUserId" to userId)
        findNavController().navigate(R.id.action_notiFragment_to_notiUserFragment, bundle)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface notiInter{
        fun photoClick(photoId:Int)
        fun commentClick(photoId:Int)
        fun userPhotoClick(userId:Int)
        fun reasonClick(notiId:Int,kind:Int, reason: String)
        fun deleteNoti(position: Int, notiId:Int)
    }

    override fun addItems(notiItems: ArrayList<NotiItem>){
        start = start + pageItems

        notiAdapter.addItemsAdapter(notiItems)
        notiAdapter.notifyDataSetChanged()

        pageLoading = false
        checkInit = true
    }

    override fun removePageLoading(){
        notiAdapter.removePageLoadingItem()
    }

    override fun clearAdapter(){
        if(swiperefresh_noti_f.isRefreshing){
            notiAdapter.clearItemsAdapter()
            notiAdapter.notifyDataSetChanged()
            swiperefresh_noti_f.isRefreshing = false
        }
    }

    override fun deleteDone(postion:Int){
        notiAdapter.notiDeleted(postion)
    }

    inner class NoificationAdapter(
        val context: Context, val notiInterListener:notiInter
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var itemsList = ArrayList<NotiItem>()

        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false

        private val SHARED = 22002
        private val FAILED = 22001
        private val COMMENT = 22003
        private val REPORTEDPHOTO = 22004
        private val REPORTEDCOMMENT = 22005

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == ITEM) { //아이템일 때 아이템뷰홀더 선택
                val view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false)
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

        fun addItemsAdapter(notiItems: ArrayList<NotiItem>){
            itemsList.addAll(notiItems)
        }

        //리싸이클러뷰 아래로 드래그시 페이징 로딩아이템 추가
        fun addPageLoadingItem() {
            isLoadingAdded = true
            addPage(NotiItem(0,0,"",0,"","",0,0,"","",""))
        }

        fun removePageLoadingItem(){
            isLoadingAdded = false
            val position = itemsList.size -1
            val item = getItem(position)

            itemsList.remove(item)
            notifyItemRemoved(position)
        }

        fun addPage(notiItem:NotiItem){
            itemsList.add(notiItem)
            notifyItemInserted(itemsList.size-1)
        }

        fun getItem(position: Int):NotiItem{
            return itemsList.get(position)
        }

        fun notiDeleted(position: Int){
            itemsList.removeAt(position)
            notifyItemRemoved(position)
        }

        override fun getItemViewType(position: Int): Int {
            if(position==itemsList.size-1 && isLoadingAdded){
                return LOADING
            }else return ITEM
        }


        @SuppressLint("StringFormatMatches")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM){
                val itemViewholder : ItemViewHolder = holder as ItemViewHolder

                fun viewVisible(imgLeft:Boolean, imgUser:Boolean, imgResult:Boolean, textReason:Boolean,
                                imgRight:Boolean){
                    itemViewholder.imgLeft.visibility = if(imgLeft) View.VISIBLE else View.GONE
                    itemViewholder.imgUser.visibility = if(imgUser) View.VISIBLE else View.GONE
                    itemViewholder.imgResult.visibility = if(imgResult) View.VISIBLE else View.GONE
                    itemViewholder.textReason.visibility = if(textReason) View.VISIBLE else View.GONE
                    itemViewholder.imgRight.visibility = if(imgRight) View.VISIBLE else View.GONE
                }

                itemsList[position].let{
                    itemViewholder.textDate.text = DateTimeFormatter.formatCreated(it.created_date)

                        when (it.kind) {
                            SHARED -> { // 업로드한 사진이 공유 되었을 때
                                viewVisible(true, false, true, false, false)

                                Glide.with(holder.itemView.context)
                                    .load(it.post_image)
                                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                    .error(android.R.drawable.stat_notify_error)
                                    .into(itemViewholder.imgLeft)

                                itemViewholder.imgResult.setImageResource(R.drawable.ic_done_white_24dp)
                                itemViewholder.imgResult.setBackgroundResource(R.drawable.img_pass)

                                itemViewholder.textMain.text =
                                    getString(R.string.notification_sharing)

                                itemViewholder.itemView.setOnClickListener {
                                    notiInterListener.photoClick(itemsList[position].post_id)
                                }
                            }

                            FAILED -> { // 업로드한 사진이 반려 되었을 때
                                viewVisible(true, false, true, true, false)
                                Glide.with(holder.itemView.context)
                                    .load(it.post_image)
                                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                    .error(android.R.drawable.stat_notify_error)
                                    .into(itemViewholder.imgLeft)

                                itemViewholder.imgResult.setImageResource(R.drawable.ic_close_white_24dp)
                                itemViewholder.imgResult.setBackgroundResource(R.drawable.img_nonpass)

                                itemViewholder.textMain.text =
                                    getString(R.string.notification_failure)
                                itemViewholder.textReason.text =
                                    getString(R.string.notification_reason, it.reason)

                                itemViewholder.itemView.setOnClickListener {
                                    notiInterListener.reasonClick(
                                        itemsList[position].id,
                                        itemsList[position].kind,
                                        itemsList[position].reason_detail
                                    )
                                }
                            }

                            COMMENT -> { // 내 게시물에 댓글이 달렸을 때
                                viewVisible(false, true, false, false, true)

                                if (it.comment_user_image == null) {
                                    itemViewholder.imgUser.setImageResource(R.drawable.img_person)
                                } else {
                                    Glide.with(holder.itemView.context)
                                        .load(it.comment_user_image)
                                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                        .error(android.R.drawable.stat_notify_error)
                                        .into(itemViewholder.imgUser)
                                }

                                itemViewholder.textMain.text =
                                    getString(R.string.text_new_comment, it.comment_user_nick)

                                Glide.with(holder.itemView.context)
                                    .load(it.post_image)
                                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                    .error(android.R.drawable.stat_notify_error)
                                    .into(itemViewholder.imgRight)

                                itemViewholder.itemView.setOnClickListener {
                                    notiInterListener.commentClick(itemsList[position].post_id)
                                }

                                itemViewholder.imgUser.setOnClickListener {
                                    notiInterListener.userPhotoClick(itemsList[position].comment_user_id)
                                }
                            }

                            REPORTEDPHOTO -> { //사진이 규칙에 위반되어 삭제되었을 때
                                viewVisible(true, false, false, true, true)

                                itemViewholder.imgLeft.setImageResource(R.drawable.ic_warning_grey_24dp)

                                itemViewholder.textMain.text = getString(
                                    R.string.was_removed_for_reason,
                                    getString(R.string.photo)
                                )
                                itemViewholder.textReason.text =
                                    getString(R.string.notification_reason, it.reason)

                                Glide.with(holder.itemView.context)
                                    .load(it.post_image)
                                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                                    .error(android.R.drawable.stat_notify_error)
                                    .into(itemViewholder.imgRight)

                                itemViewholder.itemView.setOnClickListener {
                                    notiInterListener.reasonClick(
                                        itemsList[position].id,
                                        itemsList[position].kind,
                                        itemsList[position].reason_detail
                                    )
                                }
                            }

                            REPORTEDCOMMENT -> { //댓글이 규칙에 위반되어 삭제되었을 때
                                viewVisible(true, false, false, true, false)

                                itemViewholder.imgLeft.setImageResource(R.drawable.ic_warning_grey_24dp)

                                itemViewholder.textMain.text = getString(
                                    R.string.was_removed_for_reason,
                                    getString(R.string.comment)
                                )
                                itemViewholder.textReason.text =
                                    getString(R.string.notification_reason, it.reason)

                                itemViewholder.itemView.setOnClickListener {
                                    notiInterListener.reasonClick(
                                        itemsList[position].id,
                                        itemsList[position].kind,
                                        itemsList[position].reason_detail
                                    )
                                }
                            }
                            else -> {
                            }
                        }

                                itemViewholder . itemView . isLongClickable = true
                                itemViewholder . itemView . setOnLongClickListener (object :
                            View.OnLongClickListener {
                            override fun onLongClick(v: View?): Boolean {
                                val builder = AlertDialog.Builder(context)
                                val arrayList = ArrayList<String>()
                                arrayList.add(getString(R.string.delete))
                                val listener = object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        when (which) {
                                            0 -> {  //삭제 클릭시
                                                notiInterListener.deleteNoti(
                                                    position,
                                                    itemsList[position].id
                                                )
                                            }
                                        }
                                    }
                                }
                                val adapter = ArrayAdapter<String>(
                                    context!!,
                                    android.R.layout.simple_list_item_1,
                                    arrayList
                                )
                                builder.setAdapter(adapter, listener)
                                builder.show()

                                return true
                            }
                        })


                }


            }
        }

        inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgLeft = itemView.findViewById<ImageView>(R.id.img_left_noti_i)
            val imgRight = itemView.findViewById<ImageView>(R.id.img_right_noti_i)
            val imgUser = itemView.findViewById<CircleImageView>(R.id.img_user_noti_i)
            val imgResult = itemView.findViewById<ImageView>(R.id.img_result_noti_i)
            val textMain = itemView.findViewById<TextView>(R.id.text_main_noti_f)
            val textReason = itemView.findViewById<TextView>(R.id.text_reason_noti_i)
            val textDate = itemView.findViewById<TextView>(R.id.text_date_noti_i)
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }




    }

    override fun serverError(){
        showToast(getString(R.string.server_connection_error))
    }

    fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun showFail(state:Int){
        when(state){
            1->{
                showToast(getString(R.string.failed_to_delete_noti))
            }
        }
    }


}
