package com.avon.spott

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.fragment_map_list.*


class MapListFragment: Fragment() { //MapFragment 아래에 있는 childfragment

    private val TAG = "forMapListFragment"

    //save 테스트
    private lateinit var mBundleRecyclerViewState :Bundle

    private lateinit var  recycler :RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setRetainInstance(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_map_list, container, false)
//        if(savedInstanceState==null){
//            recycler = root.findViewById<RecyclerView>(R.id.recycler_maplist_f)
//        }

        return root
    }
//
//    override fun onResume() {
//        super.onResume()
//        logd(TAG, "onResume")
//
//        if (::mBundleRecyclerViewState.isInitialized) {
//            logd(TAG, "mBundleRecyclerViewState.isInitialized")
//            val listState : Parcelable = mBundleRecyclerViewState.getParcelable("recycler_state")
//            recycler.layoutManager!!.onRestoreInstanceState(listState)
//        }
//    }
//
//    override fun onPause(){
//        super.onPause()
//        logd(TAG, "onPause")
//
//        mBundleRecyclerViewState = Bundle()
//        val listState = recycler.layoutManager!!.onSaveInstanceState()
//        mBundleRecyclerViewState.putParcelable("recycler_state", listState)
//
//    }

    override fun onStop(){
        super.onStop()
        logd(TAG, "onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logd(TAG, "onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        logd(TAG, "onDestroy()")
    }
}