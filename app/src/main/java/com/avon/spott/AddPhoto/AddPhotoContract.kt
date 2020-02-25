package com.avon.spott.AddPhoto

import android.graphics.Bitmap
import android.net.Uri
import android.text.Editable
import com.avon.spott.BaseView
import com.google.android.gms.maps.model.LatLng
import java.io.File

interface AddPhotoContract {

    interface View: BaseView<Presenter> {
        fun showToast(string: String)
        fun getPath(uri: Uri): String
        fun navigateUp()
        fun setPhoto(photo:String)
        fun movePosition(latLng: LatLng, zoom:Float)
        fun addMarker(latLng: LatLng)
        fun focusEdit()
        fun showLoading(boolean: Boolean)
        fun enableTouching(boolean: Boolean)
        fun addHashtag(hashtag:String)
        fun highlightHashtag(boolean:Boolean,editable: Editable?, start:Int, end:Int)
        fun getCursorPostion():Int
        fun showFindPlaceUi()
        fun detectEdgeUsingJNI(mInputImage:Bitmap) : File
    }

    interface Presenter{
        fun sendPhoto(baseUrl:String, cropPhoto:String,
                      photo: String, caption: String, latLng: LatLng?, hashArrayList: ArrayList<String>)
        fun navigateUp()
        fun usePhoto(photo: String)
        fun newMarker(latLng: LatLng)
        fun checkEdit(editable: Editable?)
        fun openFindPlace()
    }

}