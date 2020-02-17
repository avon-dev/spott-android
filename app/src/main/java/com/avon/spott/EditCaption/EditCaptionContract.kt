package com.avon.spott.EditCaption

import android.text.Editable
import com.avon.spott.BaseView

interface EditCaptionContract {

    interface View: BaseView<Presenter> {
        fun enableButton(isEnabled:Boolean)
        fun showToast(string: String)
        fun navigateUp()
        fun addHashtag(hashtag:String)
        fun highlightHashtag(boolean:Boolean, editable: Editable?, start:Int, end:Int)
        fun getCursorPostion():Int
    }

    interface Presenter{
        fun checkEditString(caption: String,  formerCaption:String)
        fun editCaption(baseUrl: String, photoId: Int, caption: String,hashArrayList: ArrayList<String>)
        fun navigateUp()
        fun checkEdit(editable: Editable?)
    }
}