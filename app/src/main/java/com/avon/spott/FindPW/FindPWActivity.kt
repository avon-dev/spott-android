package com.avon.spott.FindPW

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_find_pw.*
import kotlinx.android.synthetic.main.toolbar.*

class FindPWActivity : AppCompatActivity(), FindPWContract.View, View.OnClickListener {

    override lateinit var presenter: FindPWContract.Presenter
    private lateinit var findPWPresenter: FindPWPresenter

    // 유효성 검사 객체
//    private val validator by lazy { Validator.getInstance() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_pw)

        init()
    }

    override fun onDestroy() {
        CompositeDisposable().dispose()
        super.onDestroy()
    }

    private fun init() {
        findPWPresenter = FindPWPresenter(this)

        text_title_toolbar.text = getString(R.string.findpw)

        img_back_toolbar.setOnClickListener(this)

        edit_email_findpw_a.addTextChangedListener {
        }

    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
        }
    }
}
