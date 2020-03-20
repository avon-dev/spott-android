package com.avon.spott.Mypage

import android.graphics.Bitmap
import com.avon.spott.BaseView
import com.avon.spott.Data.MapCluster
import com.google.android.gms.maps.model.LatLng

interface MypageContract {

    interface View: BaseView<Presenter> {
        fun addItems(mypageItems:ArrayList<MapCluster>)
        fun showPhotoUi(id:Int)
        fun showAlarmUi()
        fun showEditMyInfoUi()
        fun showAddPhotoUi(mCropPath: String)
        fun showNoticeUi()
        fun checkPermission():Boolean
        fun showPermissionDialog()
        fun openGallery()
        fun noPhoto()
        fun movePosition(latLng: LatLng, zoom: Float)
        fun setUserInfo(nickname:String, photo:String?, isPublic:Boolean)
        fun clearAdapter()
        fun showPublic(isPublic: Boolean)
        fun showErrorToast()
        fun showToast(string: String)
        fun setNotiCount(count:Int)
    }

    interface Presenter{
        fun getMyphotos(baseUrl:String)
        fun getNotiCount(baseUrl: String, userDataChange:Boolean)
        fun openPhoto(id:Int)
        fun openAlarm()
        fun openEditMyInfo()
        fun openAddPhoto(mCropPath: String)
        fun openNotice()
        fun clickAddPhoto()
        fun changePublic(baseUrl:String, isPublic: Boolean)
    }

}