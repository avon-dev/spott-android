package com.avon.spott.Search

import com.avon.spott.BaseView
import com.avon.spott.Data.SearchItem
import com.avon.spott.Data.SearchRecent
import com.avon.spott.Data.SearchResult

interface SearchContract {
    interface View: BaseView<Presenter> {
        fun addResultItems(resultItems:ArrayList<SearchItem>)
        fun addRecentItems(recentItems:ArrayList<SearchRecent>)
        fun showUserUi(userId:Int)
        fun showHashtag(hashtag: String)
        fun showDeleteAll(show:Boolean)
    }

    interface Presenter{
        fun getSearching(baseUrl:String, text:String)
        fun openUser(userId: Int)
        fun openHashtag(hashtag:String)
    }
}