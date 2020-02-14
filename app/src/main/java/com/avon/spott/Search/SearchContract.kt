package com.avon.spott.Search

import com.avon.spott.BaseView
import com.avon.spott.Data.SearchRecent
import com.avon.spott.Data.SearchResult

interface SearchContract {
    interface View: BaseView<Presenter> {
        fun addResultItems(resultItems:ArrayList<SearchResult>)
        fun addRecentItems(recentItems:ArrayList<SearchRecent>)
        fun clearResultItems()
        fun clearRecentItems()

    }

    interface Presenter{
        fun getSearching(baseUrl:String, text:String)
    }
}