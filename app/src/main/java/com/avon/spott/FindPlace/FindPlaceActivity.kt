package com.avon.spott.FindPlace

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_find_place.*
import kotlinx.android.synthetic.main.toolbar.view.*

class FindPlaceActivity : AppCompatActivity(), FindPlaceContract.View, View.OnClickListener {

    private val TAG = "forFindPlaceActivity"

    private lateinit var findPlacePresenter: FindPlacePresenter
    override lateinit var presenter: FindPlaceContract.Presenter

    //recyclerview
//    private lateinit var findPlaceAdapter: FindPlaceAdapter
    private lateinit var layoutManager: LinearLayoutManager

    val findPlaceInterListener = object  : findPlaceInter{
        override fun itemClick() {
            //아이템 클릭
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_place)

        init()

//        val autocompleteFragment = supportFragmentManager?.findFragmentById(R.id.fragment_autocomplete) as? AutocompleteSupportFragment
//
//        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener,
//            com.google.android.libraries.places.widget.listener.PlaceSelectionListener {
//
//            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
//                logd(TAG, "place1 : $place")
//            }
//
//            override fun onPlaceSelected(place: Place?) {
//                logd(TAG, "place2 : $place")
//            }
//
//            override fun onError(status: Status) {
//                logd(TAG, "An error occurred: $status")
//            }
//
//            })


    }

    private fun init(){
        findPlacePresenter = FindPlacePresenter(this)
        include_toolbar_findplace_a.text_title_toolbar.visibility = View.GONE
        include_toolbar_findplace_a.edit_search_toolbar.visibility = View.GONE

        include_toolbar_findplace_a.img_back_toolbar.setOnClickListener(this)

        include_toolbar_findplace_a.edit_search_toolbar.addTextChangedListener {
            if(it!!.trim().length>0){
                include_toolbar_findplace_a.edit_search_toolbar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp, 0, R.drawable.ic_close_black_24dp,0)

//                presenter.getSearching(getString(R.string.baseurl), it.toString())
//                 여기에 구글 api 호출 하는 코드 넣어야함.

            }else{
                include_toolbar_findplace_a.edit_search_toolbar.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ic_search_black_24dp, 0, 0,0)
            }
        }

        include_toolbar_findplace_a.edit_search_toolbar.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                val DRAWABLE_RIGHT = 2

                when(event?.action){
                    MotionEvent.ACTION_UP ->{
                        if( include_toolbar_findplace_a.edit_search_toolbar.compoundDrawables[DRAWABLE_RIGHT]!=null){
                            if(event.rawX >= ( include_toolbar_findplace_a.edit_search_toolbar.right -  include_toolbar_findplace_a.edit_search_toolbar.compoundDrawables[DRAWABLE_RIGHT].bounds.width())){
                                include_toolbar_findplace_a.edit_search_toolbar.setText("")
                                return v?.onTouchEvent(event) ?: true
                            }
                        }

                    }
                }
                return v?.onTouchEvent(event) ?: false
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.img_back_toolbar -> { presenter.navigateUp() }
        }
    }

    override fun navigateUp() {
        onBackPressed()
    }

    interface findPlaceInter{
        fun itemClick()
    }

//    inner class FindPlaceAdapter(val context: Context, val findPlaceInter: findPlaceInter) :RecyclerView.Adapter<FindPlaceAdapter.ViewHolder>(){
//
//        private var itemsList = ArrayList<>()
//    }
}
