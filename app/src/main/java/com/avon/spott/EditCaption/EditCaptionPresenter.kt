package com.avon.spott.EditCaption

import com.avon.spott.Data.BooleanResult
import com.avon.spott.Data.CommentUpdate
import com.avon.spott.Photo.PhotoFragment.Companion.captionChange
import com.avon.spott.Utils.App
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import retrofit2.HttpException

class EditCaptionPresenter(val editCaptionView:EditCaptionContract.View):EditCaptionContract.Presenter {

    private val TAG = "EditCaptionPresenter"

    init{editCaptionView.presenter = this}

    override fun checkEditString(caption: String,  formerCaption:String) {
        if(caption.trim().length>0 && caption != formerCaption){
            editCaptionView.enableButton(true)
        }else{
            editCaptionView.enableButton(false)
        }
    }

    override fun editCaption(baseUrl: String, photoId: Int, caption: String) {
        val sending = CommentUpdate(caption)
//        logd(TAG, "photoId : "+photoId.toString())
//        logd(TAG, "Parser  : "+Parser.toJson(sending))

        Retrofit(baseUrl).patch(App.prefs.temporary_token, "/spott/posts/"+photoId.toString(), Parser.toJson(sending))
            .subscribe({ response ->
                logd(
                    TAG,
                    "response code: ${response.code()}, response body : ${response.body()}"
                )

                val string  = response.body()
                val result = Parser.fromJson<BooleanResult>(string!!)
                if(result.result){
                    editCaptionView.showToast("수정 완료") /**  성공 메세지 수정해야함.  */
                    captionChange = true
                    editCaptionView.navigateUp()
                }else{
                    editCaptionView.showToast("설명 편집에 실패했습니다")
                }
            }, { throwable ->
                logd(TAG, throwable.message)
                if (throwable is HttpException) {
                    val exception = throwable
                    logd(
                        TAG,
                        "http exception code: ${exception.code()}, http exception message: ${exception.message()}"
                    )
                    editCaptionView.showToast("서버 연결에 오류가 발생했습니다")
                }
            })


    }

    override fun navigateUp() {
        editCaptionView.navigateUp()
    }
}