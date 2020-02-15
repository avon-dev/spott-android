package com.avon.spott

import android.widget.Toast
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import android.R
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import android.widget.TextView
import android.os.Bundle



class Test {


//    private var txtSelectedPlaceName: TextView? = null
//
//    protected fun onCreate(@Nullable savedInstanceState: Bundle) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_autocomplete)
//
//        txtSelectedPlaceName = this.findViewById(R.id.txtSelectedPlaceName) as TextView
//
//        val autocompleteFragment =
//            getFragmentManager().findFragmentById(R.id.fragment_autocomplete) as PlaceAutocompleteFragment
//
//        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
//            override fun onPlaceSelected(place: Place) {
//                Log.i(LOG_TAG, "Place: " + place.name)
//                txtSelectedPlaceName!!.text =
//                    String.format("Selected places : %s  - %s", place.name, place.address)
//            }
//
//            fun onError(status: Status) {
//                Log.i(LOG_TAG, "An error occurred: $status")
//                Toast.makeText(
//                    this@AutoCompleteActivity,
//                    "Place cannot be selected!!",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        })
//
//    }
}