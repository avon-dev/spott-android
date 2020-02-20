package com.avon.spott.Reason

import com.avon.spott.BaseView

interface ReasonContract {

    interface View: BaseView<Presenter> {
        fun showGuidelineUi()
        fun showInquiryUi()
        fun setReason(photoUrl:String?, content:String)
    }

     interface Presenter {
        fun openGuideline()
        fun openInquiry()
        fun getReason(baseUrl:String, notiId:Int)
     }
}