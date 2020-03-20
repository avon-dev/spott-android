package com.avon.spott.Notice


import com.avon.spott.BaseView
import com.avon.spott.Data.Notice

interface NoticeContract {

    interface View: BaseView<Presenter> {
        fun navigateUp()
        fun addItems(noticeItems: ArrayList<Notice>)
        fun clearAdapter()
        fun showNoticeDetailUi(restUrl: String)
    }

    interface Presenter{
        fun navigateUp()
        fun openNoticeDetail(restUrl: String)
        fun getNotice(baseUrl:String)
    }

}