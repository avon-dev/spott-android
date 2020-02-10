package com.avon.spott.Photomap

import com.avon.spott.BaseView
import com.google.android.gms.maps.model.LatLng

interface PhotoMapContract {

        interface View: BaseView<Presenter> {
                fun animCamera(latLng: LatLng)
                fun showToast(string: String)
                fun checkPermission(): Boolean
                fun showPermissionDialog()
                fun showMylocation()
                fun showProgressbar(boolean: Boolean)
                fun startLocationUpdates()

                var   mylocation : LatLng?
        }

        interface Presenter{
                fun moveToSpot(latLng: LatLng)
                fun getMylocation()
        }

}