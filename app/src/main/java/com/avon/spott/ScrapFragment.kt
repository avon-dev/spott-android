package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.main.MainActivity
import com.avon.spott.main.MainActivity.Companion.mToolbar

class ScrapFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_scrap, container, false)

        return root

    }

    override fun onStart() {
        super.onStart()
        mToolbar.visibility = View.GONE
    }

}