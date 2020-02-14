package com.avon.spott.Hashtag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R

class HashtagFragment: Fragment(), HashtagContract.View, View.OnClickListener{

    private val TAG = "forHashtagFragment"

    private lateinit var hashPresenter :HashtagPresenter
    override lateinit var presenter: HashtagContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_user, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        init()
    }

    override fun onStart() {
        super.onStart()

        //툴바 처리 (뒤로가기 + 타이틀)
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.visibility = View.VISIBLE

    }

    fun init(){
        hashPresenter = HashtagPresenter(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }
}