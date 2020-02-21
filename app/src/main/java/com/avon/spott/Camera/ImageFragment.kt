package com.avon.spott.Camera


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.GalleryImage
import com.avon.spott.R
import com.bumptech.glide.Glide
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
//                    Toast.makeText(view!!.context, getString(R.string.toast_no_scrap_message), Toast.LENGTH_SHORT).show()
                    Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigateUp()
                }
            }
        }
    }

    private lateinit var broadcastManager: LocalBroadcastManager // 현재 프로세스 안에만 유효한 Broadcast. 액티비티 내부 객체간의 상호 의존성을 낮춰 깔끔한 프로그램 구조를 만들 수 있고 우리 앱의 정보를 밖으로 유출하지 않는다.

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
            Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigateUp()
        }

        val imgList = ArrayList<GalleryImage>()
        getThumbInfo(imgList)

//        recycler_image_f.layoutManager = GridLayoutManager(view.context, 4)
        recycler_image_f.layoutManager = GridLayoutManager(view.context, 4)
        recycler_image_f.adapter = ImageAdapter(view.context, imgList)

        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager = LocalBroadcastManager.getInstance(view.context)
        broadcastManager.registerReceiver(backReceiver, filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        broadcastManager.unregisterReceiver(backReceiver)
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
            val thumbAlbumCol = imageCursor.getColumnIndex(MediaStore.EXTRA_MEDIA_ALBUM)

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
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val imgView = itemView.findViewById(R.id.img_galleryimage_i) as ImageView
        }
    }

}
