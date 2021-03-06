package com.avon.spott.Search

import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.Search
import com.avon.spott.Data.SearchResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class SearchPresenter(val searchView:SearchContract.View):SearchContract.Presenter {

    private val TAG ="SearchPresenter"

    init { searchView.presenter = this }

    override fun openUser(userId: Int) {
        searchView.showUserUi(userId)
    }

    override fun openHashtag(hashtag:String){
        searchView.showHashtag(hashtag)
    }

    override fun deleteRecent(baseUrl: String, position:Int) {

        Retrofit(baseUrl).delete(App.prefs.token,"/spott/recent/"+position.toString(), "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    searchView.deleteRecentItem(position)
                }



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

    override fun deleteAll(baseUrl: String) {
        Retrofit(baseUrl).delete(App.prefs.token,"/spott/recent/-1", "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)

                if(result.result){
                    searchView.clearRecentItems()
                }

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

    override fun getRecent(baseUrl: String) {

        Retrofit(baseUrl).get(App.prefs.token,"/spott/recent", "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<SearchResult>(string!!)

                searchView.addRecentItems(result.items)


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

    override fun getSearching(baseUrl:String, text: String) {

        var search_word = text
        var tag = false
        if(text.startsWith("#")){
            tag = true
            search_word = search_word.substring(1)
        }

        val sending = Parser.toJson(Search(tag, search_word))

        logd(TAG, "sending : " + sending)

        Retrofit(baseUrl).get(App.prefs.token,"/spott/search", sending)

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<SearchResult>(string!!)

                searchView.addResultItems(result.items)


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