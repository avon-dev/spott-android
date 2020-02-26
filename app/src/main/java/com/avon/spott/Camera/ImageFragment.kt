package com.avon.spott.Camera


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.avon.spott.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_image.*

/**
 * A simple [Fragment] subclass.
 */
class ImageFragment : Fragment(), View.OnClickListener {

    // 현재 프로세스 안에만 유효한 Broadcast. 액티비티 내부 객체간의 상호 의존성을 낮춰 깔끔한 프로그램 구조를 만들 수 있고 우리 앱의 정보를 밖으로 유출하지 않는다.
//    private lateinit var broadcastManager: LocalBroadcastManager

//    private val backReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
//                KeyEvent.KEYCODE_BACK -> {
//                    activity?.onBackPressed()
//                    Toast.makeText(view!!.context, getString(R.string.toast_no_scrap_message), Toast.LENGTH_SHORT).show()
//                    Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).popBackStack()
//                }
//            }
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val url = arguments?.getString("url")

//        if(url != null) {
            Glide.with(view.context)
                .load(url)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_image_f)
//        }

        img_back_image_f.setOnClickListener(this)

//        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
//        broadcastManager = LocalBroadcastManager.getInstance(view.context)
//        broadcastManager.registerReceiver(backReceiver, filter)
    }

    override fun onDestroy() {
//        broadcastManager.unregisterReceiver(backReceiver)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.img_back_image_f -> {
                requireActivity().onBackPressed()
//                Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigateUp()
            }
        }
    }
}
