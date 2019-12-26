package com.avon.spott


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.avon.spott.main.MainActivity
import com.avon.spott.main.controlToobar
import kotlinx.android.synthetic.main.fragment_comment.view.*
import kotlinx.android.synthetic.main.toolbar.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class CommentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_comment, container, false)

        view.text_nickname_comment_f.setOnClickListener {
           findNavController().navigate(R.id.action_commentFragment_to_userFragment)
        }

        return view
//        return inflater.inflate(R.layout.fragment_comment, container, false)
    }

    override fun onStart() {
        super.onStart()
        controlToobar(View.VISIBLE, View.GONE, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
        MainActivity.mToolbar.text_title_toolbar.text = getString(R.string.comment)
        MainActivity.mToolbar.visibility = View.VISIBLE
    }


}
