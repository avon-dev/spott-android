package com.avon.spott.Camera


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.avon.spott.R
import kotlinx.android.synthetic.main.fragment_image.*

/**
 * A simple [Fragment] subclass.
 */
class ImageFragment : Fragment() {

    private val backReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                KeyEvent.KEYCODE_BACK -> {
//                    activity?.onBackPressed()
                    Toast.makeText(view!!.context, getString(R.string.toast_no_scrap_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        img_back_image_f.setOnClickListener {
            activity?.onBackPressed()
        }
    }



}
