package com.avon.spott.Search

import com.avon.spott.Data.Search
import com.avon.spott.Data.SearchResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.logd
import retrofit2.HttpException
import com.avon.spott.Utils.Retrofit

class SearchPresenter(val searchView:SearchContract.View):SearchContract.Presenter {

    private val TAG ="SearchPresenter"

    init { searchView.presenter = this }

    override fun getSearching(baseUrl:String, text: String) {
        searchView.clearResultItems()

        var search_word = text
        var tag = false
        if(text.startsWith("#")){
            tag = true
            search_word = search_word.substring(0)
        }

        val sending = Parser.toJson(Search(tag, search_word))

        logd(TAG, "sending : " + sending)

        Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/search", sending)

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<ArrayList<SearchResult>>(string!!)

                searchView.addResultItems(result)


            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
            })

    }
}