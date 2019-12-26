package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.avon.spott.main.MainActivity.Companion.mToolbar
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        ////////임시 연결///////////
        root.recycler_home_f.setOnClickListener {
            println("홈클릭 홈클릭")
//            findNavController().navigate(R.id.action_homeFragment_to_photo)
        }

        root.btn_photo_home_f.setOnClickListener { //포토아이템을 대신할 임시 포토 버튼
            println("홈 버튼 클릭 홈버튼 클릭")
            findNavController().navigate(R.id.action_homeFragment_to_photo)
        }
        /////////////////////////////////

        return root
    }

    override fun onStart() {
        super.onStart()
        mToolbar.visibility = View.GONE
    }
}
