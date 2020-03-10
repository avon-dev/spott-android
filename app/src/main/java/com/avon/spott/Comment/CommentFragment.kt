package com.avon.spott.Comment


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.Comment
import com.avon.spott.Data.UserData
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToolbar
import com.avon.spott.Utils.DateTimeFormatter.Companion.convertLocalDate
import com.avon.spott.Utils.DateTimeFormatter.Companion.formatCreated
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.dialog_comment.view.*
import kotlinx.android.synthetic.main.dialog_report.*
import kotlinx.android.synthetic.main.dialog_report.view.*
import kotlinx.android.synthetic.main.dialog_report_etc.view.*
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.toolbar.view.*


class CommentFragment : Fragment(), CommentContract.View, View.OnClickListener {

    private val TAG = "forCommentFragment"

    private lateinit var commentPresenter: CommentPresenter
    override lateinit var presenter: CommentContract.Presenter

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var layoutManager: LinearLayoutManager

    //게시글의 id
    private var photoId = 0

    //게시글 글쓴이 id
    private var userId = 0

    //페이징
    private var start: Int = 0//페이징 시작 위치
    private val pageItems = 20  // 한번에 보여지는 리사이클러뷰 아이템 수
    private var pageLoading = false // 페이징이 중복 되지 않게하기위함
    override var hasNext: Boolean = false // 다음으로 가져올 페이지가 있는지 여부
    override var refreshTimeStamp:String = "" // 서버에서 페이지를 가지고오는 시간적 준점(페이징 도중 사진이 추가될 때 중복됨을 방지)

    private var checkInit = false

    private val PHOTO = 1000
    private val NOTI = 2000

    private var fromNoti = false
    private var comeFrom = 0

    private var baseurl = ""

    private lateinit var photoCaption: String
    private var userPhoto: String? = null
    private lateinit var userNickname: String
    private lateinit var photoDateTime: String

    private var commentUploading = false

    val commentInterListener = object :commentInter{
        override fun userClick(userId:Int){
            presenter.openUser(userId)
        }

        override fun editComment(alertDialog: AlertDialog, position: Int, content: String, commentId:Int) {
            presenter.updateComment(baseurl, photoId, commentId, alertDialog, position, content)
        }

        override fun deleteComment(alertDialog: AlertDialog, position: Int, commentId:Int) {
            presenter.deleteComment(baseurl, photoId, commentId, alertDialog, position)
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

        baseurl = getString(R.string.baseurl)

        if(arguments?.getInt("photoId")!=0) {
            photoId = arguments?.getInt("photoId")!!
            userId = arguments?.getInt("userId")!!
            comeFrom = PHOTO
        }else{
            photoId = arguments?.getInt("notiPhotoId")!!
            logd(TAG, "photoId : $photoId")
            comeFrom = NOTI
            fromNoti = true
        }

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
                            presenter.getComments(baseurl, start, photoId, PHOTO)
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

                presenter.getComments(baseurl, start, photoId, PHOTO)
            }, 600) //로딩 주기
        }

        if(!checkInit) {
            //처음 사진을 가져오는 코드 (처음 이후에는 리프레쉬 전까지 가져오지않는다.)
            presenter.getComments(baseurl, start, photoId, comeFrom)

            checkInit = true
        }else{
            setPhotoData(photoCaption, userPhoto, userNickname, photoDateTime, userId)
        }



    }

    override fun onStart() {
        super.onStart()

        // 툴바 뒤로가기, 타이틀 보이게
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
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
        if(comeFrom ==PHOTO){
           setPhotoData(arguments?.getString("photoCaption").toString(),
               arguments?.getString("userPhoto"), arguments?.getString("userNickname").toString(),
               arguments?.getString("photoDateTime").toString(), userId)
        }

        edit_comment_comment_f.addTextChangedListener {
            presenter.checkEditString(it.toString())
        }

        enableSending(false)

        imgbtn_write_comment_f.setOnClickListener(this)

    }

    override fun showUserUi(userId:Int) {
        val bundle = Bundle()
        if(comeFrom ==PHOTO){
            bundle.putInt("userId", userId)
            findNavController().navigate(R.id.action_commentFragment_to_userFragment, bundle)
        }else if(comeFrom == NOTI){
            bundle.putInt("notiUserId", userId)
            findNavController().navigate(R.id.action_notiCommentFragment_to_notiUserFragment, bundle)
        }

    }

    override fun showHashtagUi(hashtag:String){
        val bundle = Bundle()
        if(comeFrom == PHOTO){
            bundle.putString("hashtag", hashtag)
            findNavController().navigate(R.id.action_commentFragment_to_hashtagFragment, bundle)
        }else if(comeFrom ==NOTI){
            bundle.putString("notiHashtag", hashtag)
            findNavController().navigate(R.id.action_notiCommentFragment_to_notiHashtagFragment, bundle)
        }

    }

    override fun setPhotoData(photoCaption:String, userPhoto:String?, userNickname:String, photoDateTime:String, userId: Int){

        this.photoCaption = photoCaption
        this.userPhoto = userPhoto
        this.userNickname =userNickname
        this.photoDateTime = photoDateTime

        if(this.userId ==0 ) {
            this.userId = userId
        }
        presenter.getHash(photoCaption)
        if(userPhoto!="" && userPhoto!= null){
            Glide.with(context!!)
                .load(userPhoto)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_profile_comment_f)
        }else{
            img_profile_comment_f.setImageResource(R.drawable.img_person)
        }

        text_nickname_comment_f.text = userNickname
        text_date_comment_f.text = photoDateTime
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.img_profile_comment_f -> {presenter.openUser(userId)}
            R.id.text_nickname_comment_f -> {presenter.openUser(userId)}
            R.id.imgbtn_write_comment_f -> {
                if(!commentUploading){
                    commentUploading = true
                    presenter.postCommnet(baseurl, photoId, edit_comment_comment_f.text.toString())
                }

            }
        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface commentInter{
        fun userClick(userId:Int)
        fun editComment(alertDialog: AlertDialog, position: Int, content: String, commentId:Int)
        fun deleteComment(alertDialog: AlertDialog, position: Int, commentId:Int)
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
            addPage(Comment(0, UserData("","",0, true),false,"",""))
        }

        fun removePageLoadingItem(){
            isLoadingAdded = false
            val position = itemsList.size -1
            val item = getItem(position)

            itemsList.remove(item)
            notifyDataSetChanged()
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

        fun commentEdited(position: Int, content:String){
            itemsList[position].contents = content
            notifyDataSetChanged()
        }

        fun commentDeleted(position: Int){
            itemsList.removeAt(position)
            notifyItemRemoved(position)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(getItemViewType(position)==ITEM) {
                val itemViewholder :ItemViewHolder = holder as ItemViewHolder
                itemsList[position].let{
                    if(it.user.profile_image!=null){
                        Glide.with(holder.itemView.context)
                            .load(it.user.profile_image)
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .into(itemViewholder.photo)
                    }else{
                        itemViewholder.photo.setImageResource(R.drawable.img_person)
                    }



                    itemViewholder.nickname.text = it.user.nickname
                    itemViewholder.content.text = it.contents
//                    itemViewholder.date.text = formatCreated(it.created)
                    itemViewholder.date.text = convertLocalDate(it.created)

                    fun editORdelete(delete:Boolean){ //댓글 수정 or 삭제
                        val mDialogView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_comment, null)
                        //AlertDialogBuilder

                        val mBuilder = AlertDialog.Builder(holder.itemView.context)
                            .setView(mDialogView)

                        val  mAlertDialog = mBuilder.show()
                        mAlertDialog.setCanceledOnTouchOutside(false)

                        if(delete){
                            mDialogView.text_warning_comment_d.visibility = View.VISIBLE
                            mDialogView.text_header_comment_d.text = getString(R.string.delete_comment)
                            mDialogView.text_done_comment_d.text = getString(R.string.delete)
                            mDialogView.text_done_comment_d.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.like_red))
                        }else{
                            mDialogView.edit_comment_d.visibility = View.VISIBLE
                            mDialogView.edit_comment_d.setText(itemViewholder.content.text)
                            mDialogView.edit_comment_d.setSelection(itemViewholder.content.text.length) //커서 뒤로 보내주는 역할
                        }

                        mDialogView.text_cancel_comment_d.setOnClickListener {
                            mAlertDialog.dismiss()
                        }

                        mDialogView.text_done_comment_d.setOnClickListener {
                            if(delete){
                                commentInterListener.deleteComment(mAlertDialog, position, itemsList[position].id)
                            }else{
                                commentInterListener.editComment(mAlertDialog, position,
                                    mDialogView.edit_comment_d.text.toString(), itemsList[position].id)
                            }
                        }
                    }

                    fun reportEtc(){
                        val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.dialog_report_etc, null)
                        //AlertDialogBuilder

                        val mBuilder = AlertDialog.Builder(context!!)
                            .setView(mDialogView)

                        mDialogView.text_header_reportetc_d.text = getString(R.string.report_comment)

                        val  mAlertDialog = mBuilder.show()
                        mAlertDialog.setCanceledOnTouchOutside(false)

                        mDialogView.edit_reportetc_d.addTextChangedListener {
                            if(it!!.trim().length>0){
                                mDialogView.btn_reportetc_d.isClickable = true
                                mDialogView.btn_reportetc_d.setBackgroundResource(R.drawable.corner_round_primary)
                            }
                        }

                        mDialogView.btn_reportetc_d.setOnClickListener {
                            presenter.report(getString(R.string.baseurl), 0, mDialogView.edit_reportetc_d.text.toString(),
                                photoId, itemsList[position].contents, itemsList[position].id, mAlertDialog, position)

                        }

                    }

                    fun report(){
                        val mDialogView = LayoutInflater.from(context!!).inflate(R.layout.dialog_report, null)
                        //AlertDialogBuilder

                        val mBuilder = AlertDialog.Builder(context!!)
                            .setView(mDialogView)

                        mDialogView.text_header_report_d.text = getString(R.string.report_comment)

                        val  mAlertDialog = mBuilder.show()
                        mAlertDialog.setCanceledOnTouchOutside(false)

                        mDialogView.radiogroup_report_d.setOnCheckedChangeListener(
                            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                                mDialogView.btn_report_d.isClickable = true
                                mDialogView.btn_report_d.setBackgroundResource(R.drawable.corner_round_primary)
                            }
                        )

                        mDialogView.btn_report_d.setOnClickListener {
                            val radioId = mDialogView.radiogroup_report_d.checkedRadioButtonId
                            val checkedRadio = mDialogView.radiogroup_report_d.findViewById<View>(radioId)
                            val index = mDialogView.radiogroup_report_d.indexOfChild(checkedRadio)

                            val detail:String
                            when(index){
                                0 ->{ detail = getString(R.string.spam) }
                                1 ->{ detail = getString(R.string.abuse_and_slander)}
                                2 -> {detail = getString(R.string.pornography)}
                                3 -> {detail = getString(R.string.unauthorized_use)}
                                else -> {detail = ""}
                            }



                            presenter.report(getString(R.string.baseurl), index+1, detail,
                                photoId, itemsList[position].contents, itemsList[position].id, mAlertDialog, position )

                        }

                        mDialogView.text_etc_report_d.setOnClickListener {
                            mAlertDialog.dismiss()
                            reportEtc()
                        }
                    }



                    itemViewholder.more.setOnClickListener {
                        val builder = AlertDialog.Builder(context)
                        val arrayList = ArrayList<String>()
                        val listener : DialogInterface.OnClickListener
                        if(itemsList[position].myself){
                            arrayList.add(getString(R.string.modify))
                            arrayList.add(getString(R.string.delete))

                            listener = object : DialogInterface.OnClickListener{
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    when(which){
                                        0 -> {  //수정 클릭시
                                            editORdelete(false)
                                        }
                                        1 -> {   //삭제 클릭시
                                            editORdelete(true)
                                        }
                                    }
                                }
                            }
                        }else{
                            arrayList.add(getString(R.string.report))

                            listener = object : DialogInterface.OnClickListener{
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    when(which){
                                        0 -> {  //신고 클릭시
                                            report()
                                        }
                                    }
                                }
                            }
                        }
                        val adapter = ArrayAdapter<String>(context!!, android.R.layout.simple_list_item_1, arrayList)
                        builder.setAdapter(adapter, listener)
                        builder.show()
                    }


                    //아이템 닉네임 클릭시 이벤트 -> 유저페이지로 이동
                    itemViewholder.nickname.setOnClickListener {
                        commentInterListener.userClick(itemsList[position].user.id)
                    }

                    //아이템 유저 사진 클릭시 이벤트 -> 유저페이지로 이동
                    itemViewholder.photo.setOnClickListener {
                        commentInterListener.userClick(itemsList[position].user.id)
                    }

                }


            }

        }

        inner class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val photo = itemView!!.findViewById<CircleImageView>(R.id.img_profile_comment_i)
            val nickname = itemView!!.findViewById<TextView>(R.id.text_nickname_comment_i) as TextView
            val content = itemView!!.findViewById<TextView>(R.id.text_content_comment_i) as TextView
            val date = itemView!!.findViewById<TextView>(R.id.text_date_comment_i) as TextView

            val more = itemView!!.findViewById<ImageButton>(R.id.imgbtn_more_comment_i)

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

    fun showToast(string: String) {
        Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
    }

    override fun postDone() {
        edit_comment_comment_f.setText("")

        swiperefresh_comment_f.isRefreshing = true
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)

        Handler().postDelayed({
            commentUploading = false
            start = 0
            refreshTimeStamp = ""
            presenter.getComments(baseurl, start, photoId, PHOTO)
        }, 600) //로딩 주기

    }

    override fun updateDone(alertDialog: AlertDialog, position: Int, content: String) {
        commentAdapter.commentEdited(position, content)
        alertDialog.dismiss()

    }

    override fun deleteDone(alertDialog: AlertDialog, position: Int){
        commentAdapter.commentDeleted(position)
        alertDialog.dismiss()
    }

    override fun setCaption(text: String) {
        text_content_comment_f.text = text
    }

    override fun setHashCaption(text:String, hashList:ArrayList<Array<Int>>){

        var spannableString = SpannableString(text)

        for (hash in hashList) {

                val start = hash[0]
                val end = hash[1]

                spannableString.setSpan(object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false

                    }
                    override fun onClick(widget: View) {
                        presenter.openHashtag(text.substring(start,end))
                    }
                }, start, end,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }

        text_content_comment_f.text = spannableString
        text_content_comment_f.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun serverError(){
        showToast(getString(R.string.server_connection_error))
    }

    override fun reportDone(alertDialog: AlertDialog, position:Int){
        showToast(getString(R.string.report_done))
        alertDialog.dismiss()
        commentAdapter.commentDeleted(position)
    }

    override fun showFailsComment(state: Int) {
        when(state){
            1->{
                commentUploading = false
                showToast(getString(R.string.failed_to_upload_comment))
            }
            2->{
                showToast(getString(R.string.failed_to_edit_comment))
            }
            3->{
                showToast(getString(R.string.failed_to_delete_comment))
            }
        }
    }





}
