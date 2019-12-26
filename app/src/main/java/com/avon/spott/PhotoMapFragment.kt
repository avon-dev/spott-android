package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.main.MainActivity
import com.avon.spott.main.MainActivity.Companion.mToolbar
import com.avon.spott.main.controlToobar
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar.view.*

class PhotoMapFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_photomap, container, false)
        return root
    }

    override fun onStart() {
        super.onStart()
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        mToolbar.text_title_toolbar.text = getString(R.string.map)
        mToolbar.visibility = View.VISIBLE
    }
}