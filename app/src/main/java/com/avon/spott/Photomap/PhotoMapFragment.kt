package com.avon.spott.Photomap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.R
import com.avon.spott.Main.MainActivity.Companion.mToolbar
import com.avon.spott.Main.controlToobar
import kotlinx.android.synthetic.main.toolbar.view.*

class PhotoMapFragment: Fragment(), PhotoMapContract.View, View.OnClickListener {

    private lateinit var photoMapPresenter: PhotoMapPresenter
    override lateinit var presenter: PhotoMapContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photomap, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    override fun onStart() {
        super.onStart()

        //툴바 뒤로가기, 타이틀 보이게
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        mToolbar.text_title_toolbar.text = getString(R.string.map)
        mToolbar.visibility = View.VISIBLE
    }

    fun init(){
        photoMapPresenter = PhotoMapPresenter(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }
}