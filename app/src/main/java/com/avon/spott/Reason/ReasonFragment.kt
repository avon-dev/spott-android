package com.avon.spott.Reason

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.Main.MainActivity
import com.avon.spott.Main.controlToolbar
import com.avon.spott.R
import com.avon.spott.inquire
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_reason.*
import kotlinx.android.synthetic.main.toolbar.view.*

class ReasonFragment :Fragment(), ReasonContract.View, View.OnClickListener{

    private val TAG = "forReasonFragment"

    private lateinit var reasonPresenter: ReasonPresenter
    override lateinit var presenter: ReasonContract.Presenter

    private var checkInit = false

    private var photoUrl :String? = null
    private var content: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root =  inflater.inflate(R.layout.fragment_reason, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
            when(arguments?.getInt("kind")!!){
                22001 -> { //반려
                    text_header_reason_f.text =  getString(R.string.notification_failure)
                    text_wrote_reason_f.text = getString(R.string.photo_i_uploaded)
                }
                22004 -> { //사진 삭제
                    text_header_reason_f.text = getString(
                        R.string.was_removed_for_reason,
                        getString(R.string.photo))
                    text_wrote_reason_f.text = getString(R.string.photo_i_uploaded)
                }

                22005 ->{ //댓글 삭제
                    text_header_reason_f.text = getString(
                        R.string.was_removed_for_reason,
                        getString(R.string.comment))
                    text_wrote_reason_f.text = getString(R.string.comment_i_wrote)
                    img_photo_reason_f.visibility = View.GONE
                }
            }

            text_reason_reason_f.text = arguments?.getString("reason")!!

        init()
    }

    fun init(){
        reasonPresenter = ReasonPresenter(this)


        val footer = text_footer_reason_f.text.toString()
        val guidelineStart = footer.indexOf(getString(R.string.guideline))
        val inquiryStart = footer.indexOf(getString(R.string.inquiry))

        val spannableString = SpannableString(footer)


        spannableString.setSpan(object :ClickableSpan(){
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
            override fun onClick(widget: View) {
                presenter.openGuideline()
            }
        }, guidelineStart, guidelineStart+getString(R.string.guideline).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(object :ClickableSpan(){
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
            override fun onClick(widget: View) {
                inquire(context!!)
            }
        }, inquiryStart, inquiryStart+getString(R.string.inquiry).length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        text_footer_reason_f.setText(spannableString)

        text_footer_reason_f.movementMethod = LinkMovementMethod.getInstance()

        if(!checkInit) {
            presenter.getReason(getString(R.string.baseurl), arguments?.getInt("notiId")!!)
            checkInit = true
        }else{
            if(photoUrl!=null){
                Glide.with(context!!)
                    .load(photoUrl)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(img_photo_reason_f)
            }
            text_contents_reason_f.text  =content
        }
    }

    override fun onStart() {
        super.onStart()
        // 툴바 뒤로가기, 타이틀 보이게
        controlToolbar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.reason)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id){

        }
    }

    override fun setReason(photoUrl: String?, content: String) {
        this.photoUrl = photoUrl
        this.content = content
        if(photoUrl!=null){
            Glide.with(context!!)
                .load(photoUrl)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_photo_reason_f)
        }
        text_contents_reason_f.text  =content
    }

    override fun showGuidelineUi() {
        /** 가이드라인 액티비티로 이동 */
    }
}