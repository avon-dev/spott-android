package com.avon.spott

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.toolbar.view.*

class OpensourceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        //툴바 타이틀 넣기
        include_toolbar_webview_a.text_title_toolbar.text = getString(R.string.text_open_source_library)

        include_toolbar_webview_a.img_back_toolbar.setOnClickListener {
            onBackPressed()
        }

        /**  오픈소스라이브러리 url 넣어야함 */
//           webview_webview_a.loadUrl("")


    }
}