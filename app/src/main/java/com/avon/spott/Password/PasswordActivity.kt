package com.avon.spott.Password

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Nickname.NicknameActivity
import com.avon.spott.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.toolbar.*

class PasswordActivity : AppCompatActivity(), PasswordContract.View, View.OnClickListener {

    lateinit var numberPresenter: PasswordPresenter
    override lateinit var presenter: PasswordContract.Presenter

//    private val validator by lazy { Validator.getInstance() }
//    private var ok: BehaviorSubject<Validator> = BehaviorSubject.createDefault(validator)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        init()
    }

    override fun onDestroy() {
        CompositeDisposable().dispose()
        super.onDestroy()
    }

    fun init() {
        numberPresenter = PasswordPresenter(this)

        text_title_toolbar.text = getString(R.string.pw)

        img_back_toolbar.setOnClickListener(this)
        btn_confirm_password_a.setOnClickListener(this)

        edit_password_a.addTextChangedListener {
//            validator.email = validator.validPassword(it.toString())
//            ok.onNext(validator)
        }

        edit_check_password_a.addTextChangedListener {
//            validator.password = edit_password_a.text.toString().equals(it.toString())
//            ok.onNext(validator)
        }

//        val okSubscribe = ok.subscribe {
//            if (it.email) text_warnmessage_password_a.visibility = View.INVISIBLE
//            else text_warnmessage_password_a.visibility = View.VISIBLE
//
//            if (it.password) text_warnmessage2_password_a.visibility = View.INVISIBLE
//            else text_warnmessage2_password_a.visibility = View.VISIBLE
//
//            if (it.valid()) {
//                btn_confirm_password_a.isClickable = true
//                btn_confirm_password_a.setBackgroundResource(R.drawable.corner_round_primary)
//            } else {
//                btn_confirm_password_a.isClickable = false
//                btn_confirm_password_a.setBackgroundResource(R.drawable.corner_round_graybtn)
//            }
//        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showNickname() {
        val intent = Intent(this@PasswordActivity, NicknameActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
            }
            R.id.btn_confirm_password_a -> {
                presenter.openNickname()
            }
        }
    }
}
