package com.avon.spott.FindPW

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.avon.spott.R
import kotlinx.android.synthetic.main.toolbar.*

class FindPWActivity : AppCompatActivity(), FindPWContract.View, View.OnClickListener {

    override lateinit var presenter: FindPWContract.Presenter
    private lateinit var findPWPresenter: FindPWPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pw)

        init()
    }

    private fun init() {
        findPWPresenter = FindPWPresenter(this)

        text_title_toolbar.text = getString(R.string.findpw)

        img_back_toolbar.setOnClickListener(this)
    }

    override fun navigateUp() { onBackPressed() }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp()}
        }
    }
}
