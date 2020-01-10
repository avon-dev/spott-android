package com.avon.spott.AddPhoto

import android.media.ExifInterface
import android.net.Uri
import com.avon.spott.Data.NewPhoto
import com.avon.spott.Utils.Parser
import com.avon.spott.Utils.Retrofit
import com.avon.spott.Utils.logd
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPhotoPresenter(val addPhotoView:AddPhotoContract.View):AddPhotoContract.Presenter {

    private val TAG = "AddPhotoPresenter"

    init{addPhotoView.presenter = this}

    override fun usePhoto(photo: String) {

        addPhotoView.setPhoto(photo) //이미지뷰에 포토 넣기

        try{
            val exifInterface = ExifInterface(addPhotoView.getPath(Uri.parse(photo)))
            val latLng = FloatArray(2)
            if(exifInterface.getLatLong(latLng)){
                logd(TAG, "photo's lat : " + latLng.get(0)) //사진의 위도
                logd(TAG, "photo's lng : " + latLng.get(1)) //사진의 경도
                val photoLatLng:LatLng = LatLng(latLng.get(0).toDouble(), latLng.get(1).toDouble())
                //사진의 위치 정보가 있으면 구글맵 카메라 이동
                addPhotoView.movePosition(photoLatLng, 16f)
                //마커생성
                addPhotoView.addMarker(photoLatLng)
            }else{
                logd(TAG, "photo has no latlng")
                //사진의 위치 정보가 없으면 구글앱 카메라 서울시청
                addPhotoView.movePosition(LatLng(37.56668, 126.97843),14f)
            }
        }catch (e: IOException){
            logd(TAG, "exifInteface IOException : " + e.localizedMessage)
        }


    }

    override fun sendPhoto(baseUrl:String, photo: String, caption: String, latLng: LatLng) {
        var images = ArrayList<MultipartBody.Part>()

        val file = File(addPhotoView.getPath(Uri.parse(photo)))
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        var requestBody: RequestBody = RequestBody.create(MediaType.parse("image/jpeg"), file)
        images.add(MultipartBody.Part.createFormData("posts_image", "p" +timeStamp +".jpg", requestBody))
        images.add(MultipartBody.Part.createFormData("back_image", "b" +timeStamp+".jpg", requestBody))

        val newPhoto = NewPhoto(latLng.latitude, latLng.longitude, caption)

        logd(TAG, "파서 테스트 : "+ Parser.toJson(newPhoto))

        Retrofit(baseUrl).postPhoto("/spott/posts", Parser.toJson(newPhoto), images).subscribe({ response ->
            logd(TAG, "response code: ${response.code()}, response body : ${response.body()}")
            val result = response.body()
            if (result != null) {
                addPhotoView.showToast("성공")
            }
        }, { throwable ->
            logd(TAG, throwable.message)
            if (throwable is HttpException) {
                val exception = throwable
                logd(
                    TAG,
                    "http exception code: ${exception.code()}, http exception message: ${exception.message()}"
                )
            }
        })

    }

    override fun navigateUp() {
        addPhotoView.navigateUp()
    }
}