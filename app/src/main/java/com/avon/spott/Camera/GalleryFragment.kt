package com.avon.spott.Camera


import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.GalleryImage
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_gallery.*

/**
 * A simple [Fragment] subclass.
 */
class GalleryFragment : Fragment() {

//    private val backReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
//                KeyEvent.KEYCODE_BACK -> {
////                    Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).popBackStack()
//                }
//            }
//        }
//    }
//
//    private lateinit var broadcastManager: LocalBroadcastManager // 현재 프로세스 안에만 유효한 Broadcast. 액티비티 내부 객체간의 상호 의존성을 낮춰 깔끔한 프로그램 구조를 만들 수 있고 우리 앱의 정보를 밖으로 유출하지 않는다.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        img_back_gallery_f.setOnClickListener {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).popBackStack(R.id.camerax_fragment, true)
            requireActivity().onBackPressed()
        }

        val imgList = ArrayList<GalleryImage>()
        getThumbInfo(imgList)

//        recycler_image_f.layoutManager = GridLayoutManager(view.context, 4)
        recycler_gallery_f.layoutManager = GridLayoutManager(view.context, 4)
        recycler_gallery_f.adapter = ImageAdapter(view.context, imgList)

//        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
//        broadcastManager = LocalBroadcastManager.getInstance(view.context)
//        broadcastManager.registerReceiver(backReceiver, filter)
    }

    override fun onDestroyView() {
//        broadcastManager.unregisterReceiver(backReceiver)
        super.onDestroyView()
    }

    override fun onDestroy() {
        logd("LifeCycle", "GalleryFragment - onDestroy()")
        super.onDestroy()
    }

    fun getThumbInfo(imgList:ArrayList<GalleryImage>) {
        val proj = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
            )

        val cr = activity!!.contentResolver
        val imageCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null, null)

        if (imageCursor != null && imageCursor.moveToFirst()) {

            val cnt = imageCursor.count

            val thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID)
            val thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            val thumbsImageNameCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            val thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
//            val thumbAlbumCol = imageCursor.getColumnIndex(MediaStore.EXTRA_MEDIA_ALBUM)

            var thumbsID:String
            var thumbsName:String
            var thumbsData:String
            var imgSize:String

            do {
                thumbsID = imageCursor.getString(thumbsIDCol)
                thumbsData = imageCursor.getString(thumbsDataCol)
                thumbsName = imageCursor.getString(thumbsImageNameCol)
                imgSize = imageCursor.getString(thumbsSizeCol)

                var galleryImage = GalleryImage(thumbsID, thumbsData, thumbsName, imgSize)
                imgList.add(galleryImage)
            } while(imageCursor.moveToNext())


            imageCursor.close()

            imgList.reverse()
        }
    }


    inner class ImageAdapter(val context: Context, val imgList:ArrayList<GalleryImage>): RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun getItemCount(): Int = imgList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_galleryimage, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(imgList[position].data)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.imgView)

            holder.itemView.setOnClickListener {
                Toast.makeText(context, imgList[position].data, Toast.LENGTH_SHORT).show()

                val bundle = Bundle()
                bundle.putString("url", imgList[position].data)

                Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigate(
                    R.id.action_gallery_to_image, bundle
                )
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val imgView = itemView.findViewById(R.id.img_galleryimage_i) as ImageView
        }
    }

}
