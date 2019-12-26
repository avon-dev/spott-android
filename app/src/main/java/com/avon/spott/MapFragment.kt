package com.avon.spott


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.main.MainActivity
import com.avon.spott.main.MainActivity.Companion.mToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.fragment_map_list.*
import kotlinx.android.synthetic.main.fragment_map_list.view.*

class MapFragment : Fragment() {

    lateinit var childfragment :Fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        configureBackdrop()

        ////임시 테스트////////////////////////////////
        root.btn_find_map_f.setOnClickListener{
            println("이 지역에서 찾기!!!!!!!!!!!!!!!!!!")
        }

        root.btn_photo_map_f.setOnClickListener {
            findNavController().navigate(R.id.action_mapFragment_to_photo)
        }
        ///////////////////////////////////////////

         return root

    }


    override fun onStart() {
        super.onStart()
        mToolbar.visibility = View.GONE

        if(mBottomSheetBehavior?.state == STATE_EXPANDED){
           bottomExpanded()
        }else{
           bottomCollapsed()
        }

    }


   var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null

    private fun configureBackdrop() {
        // Get the fragment reference
        childfragment = childFragmentManager?.findFragmentById(R.id.frag_list_map_f)!!

        childfragment?.let {
            // Get the BottomSheetBehavior from the fragment view
            BottomSheetBehavior.from(it.view)?.let { bsb ->

                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                childfragment.img_updown_maplist_f.setOnClickListener {
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
                               bottomExpanded()
                            }
                            BottomSheetBehavior.STATE_COLLAPSED -> {
                               bottomCollapsed()
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> {
                            }
                            BottomSheetBehavior.STATE_SETTLING -> {
                            }
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {

                    }
                })

            }


        }
    }

    fun bottomExpanded(){
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp)
        imgbtn_mylocation_map_f.isEnabled =false
        btn_find_map_f.isEnabled =false
    }

    fun bottomCollapsed(){
        childfragment.img_updown_maplist_f.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp)
        imgbtn_mylocation_map_f.isEnabled=true
        btn_find_map_f.isEnabled =true
    }




}









