package com.avon.spott

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_comment.*

class MainActivity : AppCompatActivity() {

    val ttlistener = object : tt {
        override fun tclick(s: String) {
            Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_comment)
        // commentFragment를 위한 테스트 코드 [Start]
        recycler_comment_f.layoutManager = LinearLayoutManager(this)
        recycler_comment_f.adapter = TAdapter(this, ttlistener)
        imgbtn_write_comment_f.setOnClickListener {
            if (swiperefresh_comment_f.isRefreshing)
                swiperefresh_comment_f.isRefreshing = false
        }
        imgbtn_profile_comment_f.setOnClickListener {
            Intent(this, CameraActivity::class.java).let {
                startActivity(it)
            }
        }
        text_content_comment_f.setOnClickListener {
            Intent(this, EditMyInfoActivity::class.java).let {
                startActivity(it)
            }
        }
    }

    interface tt {
        fun tclick(s: String)
    }

    inner class TAdapter(context: Context, var ttlistener: tt) :
        RecyclerView.Adapter<TAdapter.ViewHolder>() {
        val context = context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TAdapter.ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(holder: TAdapter.ViewHolder, position: Int) {
            holder.nickname.text = position.toString()
            holder.content.text = "#댓글 내용"
            holder.date.text = "2019년 12월 13일"

            holder.nickname.setOnClickListener {
                ttlistener.tclick(holder.nickname.text.toString())
            }
        }

        inner class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
            val nickname =
                itemView!!.findViewById<TextView>(R.id.text_nickname_comment_i) as TextView
            val content = itemView!!.findViewById<TextView>(R.id.text_content_comment_i) as TextView
            val date = itemView!!.findViewById<TextView>(R.id.text_date_comment_i) as TextView
        }
    }
    // commentFragment를 위한 테스트 코드 [End]
}