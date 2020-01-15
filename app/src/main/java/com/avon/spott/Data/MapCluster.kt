package com.avon.spott.Data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MapCluster(val latLng: LatLng, val markerTitle: String = "", val posts_image:String, val id:Int)  :
    ClusterItem {

    override fun getPosition(): LatLng {
        return latLng
    }

    override fun getTitle(): String {
        return markerTitle
    }

    override fun getSnippet(): String {
        return ""
    }
}