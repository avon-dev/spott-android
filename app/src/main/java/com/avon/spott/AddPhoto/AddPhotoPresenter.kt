package com.avon.spott.AddPhoto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Editable
import com.avon.spott.Data.NewPhoto
import com.avon.spott.Data.NewPhotoHash
import com.avon.spott.Mypage.MypageFragment.Companion.mypageChange
import com.avon.spott.Utils.*
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPhotoPresenter(val addPhotoView:AddPhotoContract.View):AddPhotoContract.Presenter {

    private val TAG = "AddPhotoPresenter"

    init{addPhotoView.presenter = this}

    override fun usePhoto(photo: String) {

        addPhotoView.setPhoto(photo) //이미지뷰에 포토 넣기

        val photoLatLng = ExifExtractor.extractLatLng(addPhotoView.getPath(Uri.parse(photo)))

        logd(TAG, "photoLatLng : " + photoLatLng  )

        if(photoLatLng==null) return //IOException 처리

        if(photoLatLng==LatLng(37.56668, 126.97843)){
            //사진의 위치 정보가 있으면 구글맵 카메라 이동(서울시청)
            addPhotoView.movePosition(photoLatLng, 14f)
        }else{
            //사진의 위치 정보가 있으면 구글맵 카메라 이동
            addPhotoView.movePosition(photoLatLng, 16f)
            //마커생성
            addPhotoView.addMarker(photoLatLng)
        }

    }

    override fun newMarker(latLng: LatLng){
        addPhotoView.addMarker(latLng)
    }

    override fun sendPhoto(baseUrl:String,  cropPhoto:String,
                           caption: String, latLng: LatLng?, hashArrayList: ArrayList<String>, lowQuality:Boolean) {
        if(latLng==null){ //사진에 대한 위치정보가 없을 때
            addPhotoView.showNoLoactionInfoToast()
        }else if(caption.trim().length==0) { //사진에 대한 설명이 없을 때 (빈공간 제외)
            addPhotoView.showNoCaptionToast()
            addPhotoView.focusEdit() //editText 포커스
        }else{
            addPhotoView.showLoading(true)
            addPhotoView.enableTouching(false)

            var images = ArrayList<MultipartBody.Part>()
            val path = addPhotoView.getPath(Uri.parse(cropPhoto))

            val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())

            val options = BitmapFactory.Options()

            if(!lowQuality){
                options.inSampleSize = 2
            }

            val bitmapPost = BitmapFactory.decodeFile(path, options)
            val filePost = addPhotoView.makeFile(bitmapPost, "post.jpg", 70, 0)
            val size = ( filePost.length()/1024).toString() //사이즈 크기 kB
            logd(TAG, "File post photo size : " + size)
            var requestBody_post: RequestBody = RequestBody.create(MediaType.parse("image/jpg"), filePost)
            images.add(
                MultipartBody.Part.createFormData( "posts_image","post" + timeStamp + ".jpg", requestBody_post)
            )


            if(!lowQuality){
                val optionsback = BitmapFactory.Options()
                optionsback.inSampleSize = 2
                val bitmapback = BitmapFactory.decodeFile(path, optionsback)
                val bitmapBack  = addPhotoView.detectEdgeUsingJNI(bitmapback)
                val fileBack = addPhotoView.makeFile(bitmapBack, "back.png", 100, 1)
                val size_back = (fileBack.length()/1024).toString() //사이즈 크기 kB
                logd(TAG, "File back photo size : " +size_back)
                var requestBody_back: RequestBody = RequestBody.create(MediaType.parse("image/png"), fileBack)
                images.add(
                    MultipartBody.Part.createFormData("back_image","back" + timeStamp + ".png",requestBody_back)
                )
            }


            var sending = ""
            if(hashArrayList.size ==0) {
                val newPhoto = NewPhoto(latLng.latitude, latLng.longitude, caption)
                sending = Parser.toJson(newPhoto)
            }else{
                var hashString = ""
                for(hash in hashArrayList){
                    hashString+= hash
                }

                val newPhotoHash =NewPhotoHash(latLng.latitude, latLng.longitude, caption, hashString)
                sending = Parser.toJson(newPhotoHash)
            }

            logd(TAG, "sending : "+sending)

            Retrofit(baseUrl).postPhoto(App.prefs.token, "/spott/posts", sending, images)
                .subscribe({ response ->
                    logd(
                        TAG,
                        "response code: ${response.code()}, response body : ${response.body()}"
                    )
                    val result = response.body()
                    if (result != null) {
                        mypageChange = true
                        addPhotoView.navigateUp()
                    }
                }, { throwable ->
                    logd(TAG, throwable.message)
                    if (throwable is HttpException) {
                        val exception = throwable
                        logd(
                            TAG,
                            "http exception code: ${exception.code()}, http exception message: ${exception.message()}"
                        )
                        addPhotoView.enableTouching(true)
                        addPhotoView.showLoading(false)
                        addPhotoView.showErrorToast()
                    }
                })


        }

    }

    override fun navigateUp() {
        addPhotoView.navigateUp()
    }

    override fun checkEdit(editable: Editable?) {

        addPhotoView.highlightHashtag(false, editable, 0, editable.toString().length)

        val matcher = Validator.validHashtag( editable.toString())
        while (matcher.find()){
            if(matcher.group(1) != "") {
                val hashtag = "#" + matcher.group(1)

                addPhotoView.addHashtag(hashtag)

                addPhotoView.highlightHashtag(true, editable, matcher.start(), matcher.end())

            }
        }


    }

    override fun openFindPlace() {
        addPhotoView.showFindPlaceUi()
    }

    override fun getMylocation() {
        if(!addPhotoView.checkPermission()){
            addPhotoView.showPermissionDialog()
            return
        }
        if(addPhotoView.mylocation !=null){
            addPhotoView.showMylocation()
            addPhotoView.animCamera(addPhotoView.mylocation!!)
        }else{
            addPhotoView.showProgressbar(true)
            addPhotoView.startLocationUpdates()
        }
    }


}