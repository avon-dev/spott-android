package com.avon.spott.TOS

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.R
import kotlinx.android.synthetic.main.fragment_webview.view.*

class PrivateFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_webview, container, false)

        /**  개인정보처리방침 url 넣어야함 */
        view.webview_webview_f.loadUrl(getString(R.string.baseurl)+"/spott/personal-Information-processing-policy")

        return view
    }
}