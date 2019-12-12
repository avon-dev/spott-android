package com.avon.spott

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.fragment_map_list.*

class MapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        configureBackdrop()

        imgbtn_mylocation_map_f.setOnClickListener {
            println("내 위치로 이동!")
            val nextIntent = Intent(this, PhotoActivity::class.java)
            startActivity(nextIntent)
        }
    }

    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    private fun configureBackdrop() {
        // Get the fragment reference
        val fragment = supportFragmentManager.findFragmentById(R.id.frag_list_map_f)

        fragment?.let {
            // Get the BottomSheetBehavior from the fragment view
            BottomSheetBehavior.from(it.view)?.let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                fragment.img_updown_maplist_f.setOnClickListener {
                    if( bsb.state == BottomSheetBehavior.STATE_COLLAPSED){
                        bsb.state = BottomSheetBehavior.STATE_EXPANDED
                    }else{
                        bsb.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                // Set the reference into class attribute (will be used latter)
                mBottomSheetBehavior = bsb


                bsb.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        // React to state change
                        when (newState) {
                            BottomSheetBehavior.STATE_HIDDEN -> {
                            }
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                fragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
                                imgbtn_mylocation_map_f.isEnabled =false
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                                fragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
                                imgbtn_mylocation_map_f.isEnabled=true
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_SETTLING -> {
                            }
                        }            }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // React to dragging events
                    }
                })

            }
        }
    }

    override fun onBackPressed() {
        // With the reference of the BottomSheetBehavior stored
        mBottomSheetBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }
}