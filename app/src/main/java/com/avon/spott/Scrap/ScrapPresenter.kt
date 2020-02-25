package com.avon.spott.Scrap

import com.avon.spott.Data.ScrapDelete
import com.avon.spott.Data.ScrapItem
import com.avon.spott.Data.ScrapResult
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class ScrapPresenter(val scrapView:ScrapContract.View) : ScrapContract.Presenter {

    private val TAG = "ScrapPresenter"

    init{ scrapView.presenter = this}

    override fun openPhoto(id: Int) { scrapView.showPhotoUi(id) }

    override fun openCamera(photoUrl: String) {
        scrapView.showCameraUi(photoUrl)
    }

    override fun getScraps(baseUrl: String) {
        Retrofit(baseUrl).get(App.prefs.temporary_token,"/spott/users/my-scrap", "")

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()
                val result = Parser.fromJson<ArrayList<ScrapResult>>(string!!)
                logd(TAG, "result!!!!! : "+result)

                scrapView.clearAdapter()

                val arrayResult = ArrayList<ScrapItem>()

                for(i in 0..result.size-1){
                    arrayResult.add(ScrapItem(result[i].post.posts_image, result[i].post.back_image, result[i].post.id))
                }
                scrapView.addItems(arrayResult)


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

    override fun deleteScraps(baseUrl: String, scrapItems:ArrayList<ScrapItem>){

        var ids = ""
        for(i in 0..scrapItems.size-1){
            if(i!=0){
                ids = ids +","
            }
            ids = ids +scrapItems[i].id
        }

        val sending = Parser.toJson(ScrapDelete(ids))

        logd(TAG, "sending은 " + sending)

        Retrofit(baseUrl).delete(App.prefs.temporary_token,"/spott/scrap/ids", sending)

            .subscribe({ response ->
                logd(TAG,"response code: ${response.code()}, response body : ${response.body()}")

                val string  = response.body()

                scrapView.deleteDone(scrapItems)

            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    logd(
                        TAG,
                        "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}"
                    )
                }
                scrapView.deleteError()
            })
    }
}