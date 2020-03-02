package com.avon.spott.EditCaption

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.avon.spott.R
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_edit_caption.*
import kotlinx.android.synthetic.main.toolbar.view.*

class EditCaptionActivity : AppCompatActivity(), EditCaptionContract.View, View.OnClickListener {

    private val TAG = "forEditCaptionActivity"

    private lateinit var editCaptionPresenter: EditCaptionPresenter
    override lateinit var presenter: EditCaptionContract.Presenter

    private var formerCaption = ""

    private val hashArrayList = ArrayList<String>() //해시태그리스트

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

        edit_caption_editcaption_a.addTextChangedListener{
            presenter.checkEditString(edit_caption_editcaption_a.text.toString(), formerCaption)

            hashArrayList.clear()
            presenter.checkEdit(it)
        }

        edit_caption_editcaption_a.setText(formerCaption)
        edit_caption_editcaption_a.setSelection(formerCaption.length) //커서 뒤로 보내주는 역할

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btn_edit_editcaption_a -> {
                presenter.editCaption(getString(R.string.baseurl), intent.getIntExtra("photoId",0), edit_caption_editcaption_a.text.toString(), hashArrayList)
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

    override fun addHashtag(hashtag:String){
        hashArrayList.add(hashtag)
        logd("hashtest","hashtest : "+hashArrayList)
    }

    override fun highlightHashtag(boolean:Boolean, editable: Editable?, start:Int, end:Int){ //해시태그 하이라이트 켜고 끄는 함수

        if(boolean){
            editable!!.setSpan(
                BackgroundColorSpan(ContextCompat.getColor(this, R.color.hashtag_highlight)),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }else{
            val spans = editable!!.getSpans(0,editable.toString().length, BackgroundColorSpan::class.java)
            for(span in spans){
                editable!!.removeSpan(span)
            }
        }

    }

    override fun getCursorPostion():Int{ //EditText의 현재 커서 위치
        return edit_caption_addphoto_a.selectionEnd
    }

    override fun failedToEditCaption() {
        showToast(getString(R.string.failed_to_edit_caption))
    }

    override fun showErrorToast() {
        showToast(getString(R.string.server_connection_error))
    }
}
