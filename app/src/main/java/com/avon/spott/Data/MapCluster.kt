package com.avon.spott.Data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MapCluster(val latitude:Double, val longitude:Double, val posts_image: String, val id:Int,
                      val like_count:Int, val post_kind:Int) :ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }
}