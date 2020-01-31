package com.avon.spott

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import com.avon.spott.Data.MapCluster
import com.avon.spott.Map.MapFragment.Companion.mBottomSheetBehavior
import com.avon.spott.Map.MapFragment.Companion.selectedMarker
import com.avon.spott.Mypage.MypageFragment.Companion.mapRecyclerView
import com.avon.spott.Mypage.MypageFragment.Companion.selectedMarkerMypage
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator

class PhotoRenderer(mcontext: Context, mMap:GoogleMap, clusterManager: ClusterManager<MapCluster>, mapFrag:Boolean)
    : DefaultClusterRenderer<MapCluster>(mcontext, mMap, clusterManager){

    private val context = mcontext
    private val mapFrag:Boolean = mapFrag
    private val TAG = "PhotoRenderer"

    val customMarkerView: View
    private val mClusterIconGenerator = IconGenerator(context)

    private val icon: Bitmap

    init{
        customMarkerView = LayoutInflater.from(context).inflate(R.layout.marker_photo, null)
        mClusterIconGenerator.setContentView(customMarkerView)

        //임시로 불러와지는 비트맵을 만든다. -> empty bitmap (임시 비트맵을 안만들면 구글 기본 빨간 마커가 보인다.)
        var conf: Bitmap.Config = Bitmap.Config.ARGB_8888 // see other conf types
        var bmp = Bitmap.createBitmap(50, 50, conf) // this creates a MUTABLE bitmap

        icon = bmp
    }

    override fun onBeforeClusterItemRendered(item: MapCluster?, markerOptions: MarkerOptions?) {
        //클러스터아이템 만들어지기 전 임시로 나오는 아이콘 지정 -> empty bitmap
        markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterItemRendered(clusterItem: MapCluster?, marker: Marker?) {
        if(mapFrag){
            selectedMarker = null //선택된 클러스터 null로 만듦. -> 새로 클러스터링이 되면 선택했던 클러스터 없어지게함.
            mBottomSheetBehavior?.state =  BottomSheetBehavior.STATE_COLLAPSED //리스트플래그먼트는 내려가게함.
        }else{
            if( selectedMarkerMypage != null) {
                animSlide(context, mapRecyclerView, false)
                selectedMarkerMypage = null
            }
        }


        Glide.with(context)
            .asBitmap()
            .load(clusterItem!!.posts_image)
            .fitCenter()
            .into(object : CustomTarget<Bitmap>(){

                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    marker!!.setIcon(BitmapDescriptorFactory.fromBitmap(
                        getMarkerBitmapFromView(customMarkerView, bitmap, 1,false, context)))
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    logd(TAG, "onLoadFailed" + errorDrawable)
                }
            })
    }


    override fun onBeforeClusterRendered(cluster: Cluster<MapCluster>?, markerOptions: MarkerOptions?) {
        //클러스터 만들어지기 전 임시로 나오는 아이콘 지정 -> empty bitmap
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterRendered(cluster: Cluster<MapCluster>?, marker: Marker?) {
        if(mapFrag) {
            selectedMarker = null//선택된 클러스터 null로 만듦. -> 새로 클러스터링이 되면 선택했던 클러스터 없어지게함.
            mBottomSheetBehavior?.state =
                BottomSheetBehavior.STATE_COLLAPSED //맵리스트플래그먼트(하단플래그먼트)는 내려가게함.
        }else{
            if( selectedMarkerMypage != null) {
                animSlide(context, mapRecyclerView, false)
                selectedMarkerMypage = null
            }
        }

        val firstItem = cluster!!.items.iterator().next()   //첫번째 아이템 선택

        //클러스터 순서 테스트중....
        logd(TAG, "----------------------------------------------------")
        logd(TAG, "size  : "+cluster!!.items.size.toString())
        logd(TAG, "first : " +cluster!!.items.iterator().next())
        logd(TAG, "all   : " +cluster.items)


        Glide.with(context)
            .asBitmap()
            .load(firstItem.posts_image)
            .fitCenter()
            .into(object :CustomTarget<Bitmap>(){
                override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?){
                    marker!!.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            getMarkerBitmapFromView(customMarkerView,bitmap, cluster.size, false, context)))
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    logd(TAG, "onLoadFailed" + errorDrawable)
                }

            })
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MapCluster>?): Boolean {
        // 언제나 클러스터가 일어나게함. 클러스터아이템이 2개 이상이면 클러스터가 일어나게함.
        return cluster!!.size > 1
    }
}