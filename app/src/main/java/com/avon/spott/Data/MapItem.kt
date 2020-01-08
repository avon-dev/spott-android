package com.avon.spott.Data

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class MapItem(val latLng: LatLng, val markerTitle: String = "", val imgUrl:String)  : ClusterItem{

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