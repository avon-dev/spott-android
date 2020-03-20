package com.avon.spott

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.activity_webview.*
import kotlinx.android.synthetic.main.toolbar.view.*
import java.util.*

class GuidelineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        //툴바 타이틀 넣기
        include_toolbar_webview_a.text_title_toolbar.text = getString(R.string.guideline)

        include_toolbar_webview_a.img_back_toolbar.setOnClickListener {
            onBackPressed()
        }

        if(Locale.getDefault().language=="ko"){
            webview_webview_a.loadUrl(getString(R.string.baseurl)+"/spott/guide")
        }else if(Locale.getDefault().language=="zh"){
            webview_webview_a.loadUrl(getString(R.string.baseurl)+"/spott/guide-cn")
        }else{
            webview_webview_a.loadUrl(getString(R.string.baseurl)+"/spott/guide-en")
        }




    }
}