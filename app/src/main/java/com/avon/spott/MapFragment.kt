package com.avon.spott

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map_list.*

class MapFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root

        configureBackdrop()

    }



    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    private fun configureBackdrop() {
//        // Get the fragment reference
//        val fragment = supportFragmentManager.findFragmentById(R.id.frag_list_map_f)
//
//        fragment?.let {
//            // Get the BottomSheetBehavior from the fragment view
//            BottomSheetBehavior.from(it.view)?.let { bsb ->
//                // Set the initial state of the BottomSheetBehavior to HIDDEN
//                bsb.state = BottomSheetBehavior.STATE_HIDDEN
//
//                fragment.img_updown_maplist_f.setOnClickListener {
//                    if( bsb.state == BottomSheetBehavior.STATE_COLLAPSED){
//                        bsb.state = BottomSheetBehavior.STATE_EXPANDED
//                    }else{
//                        bsb.state = BottomSheetBehavior.STATE_COLLAPSED
//                    }
//                }
//
//                // Set the reference into class attribute (will be used latter)
//                mBottomSheetBehavior = bsb
//
//
//                bsb.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                    override fun onStateChanged(bottomSheet: View, newState: Int) {
//                        // React to state change
//                        when (newState) {
//                            BottomSheetBehavior.STATE_HIDDEN -> {
//                            }
//                            BottomSheetBehavior.STATE_EXPANDED -> {
//                                fragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
//                                imgbtn_mylocation_map_f.isEnabled =false
//                                btn_find_map_f.isEnabled =false
//
//                            }
//                            BottomSheetBehavior.STATE_COLLAPSED -> {
//                                fragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
//                                imgbtn_mylocation_map_f.isEnabled=true
//                                btn_find_map_f.isEnabled =true
//                            }
//                            BottomSheetBehavior.STATE_DRAGGING -> {
//                            }
//                            BottomSheetBehavior.STATE_SETTLING -> {
//                            }
//                        }
//                    }
//
//                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
//
//                    }
//                })
//
//            }
//        }
    }

}









