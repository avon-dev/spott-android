package com.avon.spott.EditCaption

import com.avon.spott.BaseView

interface EditCaptionContract {

    interface View: BaseView<Presenter> {
        fun enableButton(isEnabled:Boolean)
        fun showToast(string: String)
        fun navigateUp()
    }

    interface Presenter{
        fun checkEditString(caption: String,  formerCaption:String)
        fun editCaption(baseUrl:String, photoId : Int, caption: String)
        fun navigateUp()
    }
}