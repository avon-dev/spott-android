package com.avon.spott.Search

import com.avon.spott.BaseView
import com.avon.spott.Data.SearchItem

interface SearchContract {
    interface View: BaseView<Presenter> {
        fun addResultItems(resultItems:ArrayList<SearchItem>)
        fun addRecentItems(recentItems:ArrayList<SearchItem>)
        fun deleteRecentItem(position: Int)
        fun clearRecentItems()
        fun showUserUi(userId:Int)
        fun showHashtag(hashtag: String)
        fun showDeleteAll(show:Boolean)
    }

    interface Presenter{
        fun deleteRecent(baseUrl: String, recentId:Int, postion:Int)
        fun deleteAll(baseUrl: String)
        fun getRecent(baseUrl: String)
        fun getSearching(baseUrl:String, text:String)
        fun openUser(userId: Int)
        fun openHashtag(hashtag:String)
    }
}