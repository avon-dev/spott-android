package com.avon.spott.Comment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.R
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToobar
import kotlinx.android.synthetic.main.fragment_comment.*
import kotlinx.android.synthetic.main.fragment_comment.view.*
import kotlinx.android.synthetic.main.toolbar.view.*

// -------↓ 아래 코드들은 몰라서 일단 나둠 (민석). -------------------------
// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
//-----------------------------------------------------------------------

class CommentFragment : Fragment(), CommentContract.View, View.OnClickListener {

    private lateinit var commentPresenter: CommentPresenter
    override lateinit var presenter: CommentContract.Presenter

    val commentInterListener = object :commentInter{
        override fun itemClick(){
            presenter.openPhoto()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_comment, container, false)

        //------[임시]swiperefreshlayout 컨트롤용------------------
        root.imgbtn_write_comment_f.setOnClickListener {
            if (swiperefresh_comment_f.isRefreshing)
                swiperefresh_comment_f.isRefreshing = false
        }
        //-----------------------------------------------------

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()

        //---------리사이클러뷰 테스트 코드--------------------------------
        val layoutManager = LinearLayoutManager(context!!)

        recycler_comment_f.layoutManager = layoutManager
        recycler_comment_f.adapter = CommentAdapter(context!!, commentInterListener)
        //------------------------------------------------------------
    }

    override fun onStart() {
        super.onStart()

        // 툴바 뒤로가기, 타이틀 보이게
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.comment)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    fun init(){
        commentPresenter = CommentPresenter(this)
        text_nickname_comment_f.setOnClickListener(this)
    }

    override fun showPhotoUi() {
        findNavController().navigate(R.id.action_commentFragment_to_userFragment)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.text_nickname_comment_f -> {presenter.openPhoto()}
        }
    }

    //리사이클러뷰 아이템 클릭을 위한 인터페이스
    interface commentInter{
        fun itemClick()
    }

    inner class CommentAdapter(
        val context: Context, val commentInterListener:commentInter
    ):RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CommentAdapter.ViewHolder {
            val view =  LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
            holder.nickname.text = "UserNickname"+position.toString()
            holder.content.text = "#댓글 내용"
            holder.date.text = "2019년 12월 13일"

            holder.nickname.setOnClickListener {
                commentInterListener.itemClick()
            }
        }

        inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val nickname = itemView!!.findViewById<TextView>(R.id.text_nickname_comment_i) as TextView
            val content = itemView!!.findViewById<TextView>(R.id.text_content_comment_i) as TextView
            val date = itemView!!.findViewById<TextView>(R.id.text_date_comment_i) as TextView
        }

    }


}
