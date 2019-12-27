package com.avon.spott.Nickname

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.avon.spott.MainActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_nickname.*
import kotlinx.android.synthetic.main.toolbar.*

class NicknameActivity : AppCompatActivity(), NicknameContract.View, View.OnClickListener {

    override lateinit var presenter: NicknameContract.Presenter
    private lateinit var nicknamePresenter:NicknamePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        init()
    }

    private fun init() {
        nicknamePresenter = NicknamePresenter(this)

        text_title_toolbar.text = getString(R.string.nickname)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_nickname_a.setOnClickListener(this)
    }

    override fun navigateUp() { onBackPressed() }

    override fun showMainUi() {
        val intent = Intent(this@NicknameActivity, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp() }
            R.id.btn_confirm_nickname_a -> { presenter.openMain() }
        }
    }
}
