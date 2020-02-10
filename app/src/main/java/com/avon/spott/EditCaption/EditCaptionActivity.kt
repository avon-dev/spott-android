package com.avon.spott.EditCaption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_edit_caption.*
import kotlinx.android.synthetic.main.toolbar.view.*

class EditCaptionActivity : AppCompatActivity(), EditCaptionContract.View, View.OnClickListener {

    private val TAG = "forEditCaptionActivity"

    private lateinit var editCaptionPresenter: EditCaptionPresenter
    override lateinit var presenter: EditCaptionContract.Presenter

    private var formerCaption = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_caption)

        formerCaption = intent.getStringExtra("caption")

        init()
    }

    private fun init(){
        editCaptionPresenter = EditCaptionPresenter(this)
        btn_edit_editcaption_a.setOnClickListener(this)
        include_toolbar_editcaption_a.img_back_toolbar.setOnClickListener(this)


        //툴바 타이틀 넣기
        include_toolbar_editcaption_a.text_title_toolbar.text = getString(R.string.editing_caption)

        edit_caption_editcaption_a.setText(formerCaption)
        edit_caption_editcaption_a.setSelection(formerCaption.length) //커서 뒤로 보내주는 역할

        edit_caption_editcaption_a.addTextChangedListener{
            presenter.checkEditString(edit_caption_editcaption_a.text.toString(), formerCaption)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_edit_editcaption_a -> {
                presenter.editCaption(getString(R.string.testurl), intent.getIntExtra("photoId",0), edit_caption_editcaption_a.text.toString())
            }
            R.id.img_back_toolbar ->{ presenter.navigateUp() }
        }

    }

    override fun enableButton(isEnabled:Boolean){
        btn_edit_editcaption_a.isEnabled = isEnabled
        btn_edit_editcaption_a.setBackgroundResource(if(isEnabled) R.drawable.corner_round_primary
        else R.drawable.corner_round_graybtn)
    }

    override fun showToast(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    override fun navigateUp() {
        onBackPressed()
    }
}
