package com.avon.spott.Comment


import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.Comment
import com.avon.spott.Data.NickPhoto
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_comment.view.*
import kotlinx.android.synthetic.main.toolbar.view.*


class CommentFragment : Fragment(), CommentContract.View, View.OnClickListener {

    private val TAG = "forCommentFragment"

    private lateinit var commentPresenter: CommentPresenter
    override lateinit var presenter: CommentContract.Presenter

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //게시글의 id
    private var photoId = 0

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 20  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함
    override var hasNext: Boolean = false // 다음으로 가져올 페이지가 있는지 여부
    override var refreshTimeStamp:String = "" // 서버에서 페이지를 가지고오는 시간적 준점(페이징 도중 사진이 추가될 때 중복됨을 방지)

    private var checkInit = false

    val commentInterListener = object :commentInter{
        override fun userClick(){
            presenter.openPhoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        layoutManager = LinearLayoutManager(context!!)
        commentAdapter = CommentAdapter(context!!, commentInterListener)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_comment, container, false)

//        //------[임시]swiperefreshlayout 컨트롤용------------------
//        root.imgbtn_write_comment_f.setOnClickListener {
//            if (swiperefresh_comment_f.isRefreshing)
//                swiperefresh_comment_f.isRefreshing = false
//        }
//        //-----------------------------------------------------

        photoId = arguments?.getInt("photoId")!!

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        recycler_comment_f.layoutManager = layoutManager
        recycler_comment_f.adapter = commentAdapter

        recycler_comment_f.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(!recycler_comment_f.canScrollVertically(1)){
                    logd("ScrollTEST","work")
                    if(!pageLoading && hasNext){
                        logd("ScrollTEST","END")
                        pageLoading = true
                        commentAdapter.addPageLoadingItem() //리싸이클러뷰에 로딩아이템 생성 생성

                        recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount-1)

                        Handler().postDelayed({
                            presenter.getComments(getString(R.string.testurl), start, photoId)
                        }, 400) //로딩 주기
                    }
                }
            }
        })

        swiperefresh_comment_f.setOnRefreshListener {
            Handler().postDelayed({
                logd(TAG, "refreshed!!!")

                //페이징 시작위치와 시간 초기화
                start = 0
                refreshTimeStamp = ""

                presenter.getComments(getString(R.string.testurl), start, photoId)
            }, 600) //로딩 주기
        }

        if(!checkInit) {
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
            presenter.getComments(getString(R.string.testurl), start, photoId)

            checkInit = true
        }


    }

    override fun onStart() {
        super.onStart()

        // 툴바 뒤로가기, 타이틀 보이게
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.comment)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recycler_comment_f.layoutManager = null
    }

    fun init(){
        commentPresenter = CommentPresenter(this)
        text_nickname_comment_f.setOnClickListener(this)

        //스와이퍼리프레쉬 레이아웃 색상변경
        swiperefresh_comment_f.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.colorPrimary))


        //PhotoFragment에서 넘어온 게시글 데이터
        if(arguments?.getString("userPhoto")!=""){
            Glide.with(context!!)
                .load(arguments?.getString("userPhoto"))
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_profile_comment_f)
        }else{
            img_profile_comment_f.setImageResource(R.drawable.ic_account_circle_grey_36dp)
        }

        text_nickname_comment_f.text = arguments?.getString("userNickname").toString()
        text_content_comment_f.text = arguments?.getString("photoCaption").toString()
        text_date_comment_f.text = arguments?.getString("photoDateTime").toString()

        edit_comment_comment_f.addTextChangedListener {
            presenter.checkEditString(it.toString())
        }

        enableSending(false)

        imgbtn_write_comment_f.setOnClickListener(this)
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_commentFragment_to_userFragment)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.text_nickname_comment_f -> {presenter.openPhoto()}
            R.id.imgbtn_write_comment_f -> {presenter.postCommnet(getString(R.string.testurl),
                photoId, edit_comment_comment_f.text.toString())}
        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface commentInter{
        fun userClick()
    }

    override fun addItems(commentItems: ArrayList<Comment>) {
        start = start +pageItems

        commentAdapter.addItemsAdapter(commentItems)
        commentAdapter.notifyDataSetChanged()

        pageLoading = false
    }

    override fun removePageLoading(){
        commentAdapter.removePageLoadingItem()
    }

    override fun clearAdapter(){
        if (swiperefresh_comment_f.isRefreshing) {
            commentAdapter.clearItemsAdapter()
            commentAdapter.notifyDataSetChanged()
            swiperefresh_comment_f.isRefreshing = false
        }
    }

    inner class CommentAdapter(
        val context: Context, val commentInterListener:commentInter
    ):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        private var itemsList = ArrayList<Comment>()
        val ITEM = 0
        val LOADING = 1
        private var isLoadingAdded = false


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            if(viewType==ITEM){
                val view =  LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
                return ItemViewHolder(view)
            }else{
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

        fun addItemsAdapter(commnetItems: ArrayList<Comment>){
            itemsList.addAll(commnetItems)
        }

        //리싸이클러뷰 아래로 드래그시 페이징 로딩아이템 추가
        fun addPageLoadingItem() {
            isLoadingAdded = true
            addPage(Comment(0, NickPhoto("",""),false,"",""))
        }

        fun removePageLoadingItem(){
            isLoadingAdded = false
            val position = itemsList.size -1
            val item = getItem(position)

            itemsList.remove(item)
            notifyItemRemoved(position)
        }

        fun addPage(commentItem:Comment){
            itemsList.add(commentItem)
            notifyItemInserted(itemsList.size-1)
        }

        fun getItem(position: Int):Comment{
            return itemsList.get(position)
        }

        override fun getItemViewType(position: Int): Int {
            if(position==itemsList.size-1 && isLoadingAdded){
                return LOADING
            }else return ITEM
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM) {
                val itemViewholder :ItemViewHolder = holder as ItemViewHolder
                itemsList[position].let{
                    Glide.with(holder.itemView.context)
                        .load(it.user.profile_image)
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(itemViewholder.photo)

                    itemViewholder.nickname.text = it.user.nickname
                    itemViewholder.content.text = it.contents
                    itemViewholder.date.text = it.created

                    if(it.myself){
                        itemViewholder.editor.visibility = View.VISIBLE
                        itemViewholder.remover.visibility = View.VISIBLE
                    }
                }

                //아이템 닉네임 클릭시 이벤트 -> 유저페이지로 이동
                itemViewholder.nickname.setOnClickListener {
                    commentInterListener.userClick()
                }

                //아이템 유저 사진 클릭시 이벤트 -> 유저페이지로 이동
                itemViewholder.photo.setOnClickListener {
                    commentInterListener.userClick()
                }

                //아이템 수정하기 클릭시
                itemViewholder.editor.setOnClickListener{

                }

                //아이템 삭제하기 클릭시
                itemViewholder.remover.setOnClickListener{

                }

            }else{

            }

        }

        inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val photo = itemView!!.findViewById<CircleImageView>(R.id.img_profile_comment_i)
            val nickname = itemView!!.findViewById<TextView>(R.id.text_nickname_comment_i) as TextView
            val content = itemView!!.findViewById<TextView>(R.id.text_content_comment_i) as TextView
            val date = itemView!!.findViewById<TextView>(R.id.text_date_comment_i) as TextView

            val editor = itemView!!.findViewById<ImageButton>(R.id.imgbtn_edit_comment_i)
            val remover = itemView!!.findViewById<ImageButton>(R.id.imgbtn_remove_comment_i)
        }

        inner class LoadingViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        }

    }

    override fun enableSending(boolean: Boolean) {
        logd(TAG, "enableSending : " + boolean)
        imgbtn_write_comment_f.isEnabled = boolean
        imgbtn_write_comment_f.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context!!,
            if(boolean) R.color.colorPrimary else R.color.background_grey)))
    }

    override fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun postDone() {
        edit_comment_comment_f.setText("")

        start = 0
        refreshTimeStamp = ""
        swiperefresh_comment_f.isRefreshing = true
        presenter.getComments(getString(R.string.testurl), start, photoId)

    }





}
