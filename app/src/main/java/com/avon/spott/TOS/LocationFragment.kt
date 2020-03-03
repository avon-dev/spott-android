package com.avon.spott.TOS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.R
import kotlinx.android.synthetic.main.fragment_webview.view.*

class LocationFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)

        /**  위치기반서비스 이용약관 url 넣어야함 */
//        view.webview_webview_f.loadUrl("")

        return view
    }
}