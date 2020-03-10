package com.avon.spott.Camera


import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.Metadata
import androidx.camera.core.ImageCapture.OnImageSavedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Data.ScrapItem
import com.avon.spott.Data.ScrapResult
import com.avon.spott.R
import com.avon.spott.Utils.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_camera_x.*
import retrofit2.HttpException
import java.io.File
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

typealias LumaListener = (luma: Double) -> Unit

class CameraXFragment : Fragment() {

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var outputDirectory: File
//    private lateinit var broadcastManager: LocalBroadcastManager // 현재 프로세스 안에만 유효한 Broadcast. 액티비티 내부 객체간의 상호 의존성을 낮춰 깔끔한 프로그램 구조를 만들 수 있고 우리 앱의 정보를 밖으로 유출하지 않는다.
    private lateinit var displayManager: DisplayManager
    private lateinit var mainExecutor: Executor
//    private lateinit var analysisExecutor: Executor

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    //    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    // 사진 찍을 때 쓸 볼륨 다운 버튼 리시버
//    private val volumeReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
//                KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
//                    val shutter = container.findViewById<ImageButton>(R.id.imgbtn_shoot_camerax_f)
//                    shutter.simulateClick()
//                }
//            }
//        }
//    }

    // 구성 변경을 유발하지 않는 방향 변경에 대해 디스플레이리스너가 필요하다.
    // 예를 들어 매니페스트에서 구성 변경 또는 180도 방향 변경에 대해 재정의하도록 선택한 경우
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) =
            Unit // Unit 함수의 반환 구문이 없다는 것을 표현, Nothing 의미있는 데이터가 없다는 것을 명시적으로 선언하기 위해 사용

        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraXFragment.displayId) {
                logd(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.setTargetRotation(view.display.rotation)
//                imageAnalyzer?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }


    private lateinit var recyclerview: RecyclerView
    private lateinit var overlayImage: ImageView
    private lateinit var closeImage: ImageView
    private lateinit var opacitySeekbar: SeekBar

    private lateinit var adapter:CameraXAdapter

    private lateinit var rightRotation:ImageView

    // 뒤로가기 키 리시버
//    private val backReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
//                KeyEvent.KEYCODE_BACK -> {
//                    if (overlayImage.isVisible)
//                        hideOverlayImage()
//                    else if (recyclerview.isVisible)
//                        recyclerview.visibility = View.INVISIBLE
//                    else
//                        activity?.onBackPressed()
//                }
//            }
//        }
//    }


    private var currentPhoto:Int = 0
        get
        set

    private val BACK_IMAGE = 1
    private val POST_IMAGE = 0
    private var photoUrl:Array<String?>? = null

    // 리사이클러뷰 어댑터 클릭 리스너
    interface OnItemClickListener {
        fun ItemClick(photoUrl: Array<String?>?)
    }

    private val onItemClickListener = object : OnItemClickListener {
        override fun ItemClick(photoUrl: Array<String?>?) {

            if(photoUrl == null) return

            this@CameraXFragment.photoUrl = photoUrl

//            this@CameraXFragment.scrapItem = scrapItem

            if (overlayImage.isVisible) {// 이미지 숨기기
                hideOverlayImage()
            }
//            else { // 리사이클러 뷰에서 선택한 아이템 보여주기

                var hasBack:Boolean

                if(photoUrl[BACK_IMAGE]!= null) { // 백 이미지 있을 때, 이미지 변환이 가능함 (frame layout이 보여짐)

                    currentPhoto = BACK_IMAGE
                    hasBack = true
                    Glide.with(overlayImage)
                        .load(photoUrl[BACK_IMAGE])
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(overlayImage)
                } else { // 백 이미지 없을 때, 이미지 변환을 못함(frame layout이 보여지면 안됨)
                    currentPhoto = POST_IMAGE
                    hasBack = false
                    Glide.with(overlayImage)
                        .load(photoUrl[POST_IMAGE])
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(overlayImage)
                }


//            }

              recyclerview.visibility = View.GONE /** 리싸이클러뷰 숨기기 추가  */

                showOverlayImage(hasBack)
//            }
        }
    }

//    private var scrapItem:ScrapItem? = null
    private lateinit var onBackPressedCallback:OnBackPressedCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainExecutor =
            ContextCompat.getMainExecutor(requireContext()) // requireContext() : fragment 수명주기와 관련하여 사용해야 함.
//        analysisExecutor = Executors.newSingleThreadExecutor()

//        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        // 메인 액티비티로부터 받을 인텐트 필터 이벤트 설정
//        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
//        broadcastManager.registerReceiver(volumeReceiver, filter)
//        broadcastManager.registerReceiver(backReceiver, filter)
    }

    override fun onResume() {
        super.onResume()

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigate(
                CameraXFragmentDirections.actionCameraToPermissions()
            )
        }

        onBackPressedCallback.isEnabled = true

    }

    override fun onDestroyView() {
        // 브로드캐스트 리시버와 리스너 해제
//        broadcastManager.unregisterReceiver(volumeReceiver) // 동적 리시버는 해제하지 않을시 메모리 누수가 발생하므로 꼭 해준다
//        broadcastManager.unregisterReceiver(backReceiver)
        displayManager.unregisterDisplayListener(displayListener)

        super.onDestroyView()
    }

    override fun onDestroy() {
        logd("LifeCycle", "CameraXActivity - onDestroy()")

        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_camera_x, container, false)


    private fun setGalleryThumbnail(file: File) {
        // 갤러리 미리보기를 고정하는 뷰
        val thumbnail = container.findViewById<CircleImageView>(R.id.imgbtn_gallery_camerax_f)

        // view스레드 작업을 실행
        thumbnail.post {
            // thumbnail padding 지우기
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())

            Glide.with(thumbnail)
                .load(file)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail)

        }
    }


    // 사진을 찍고 디스크에 저장한 후 트리거되는 콜백 정의
    private val imageSavedListener = object : OnImageSavedCallback {
        override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
            loge(TAG, "Photo capture failed: $message", cause)
        }

        override fun onImageSaved(photoFile: File) {
            logd(TAG, "Photo capture succeeded: ${photoFile.absoluteFile}")

            // API Level 23이상에서만 foreground Drawable을 변경할 수 있다.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setGalleryThumbnail(photoFile)
            }

            // API Level 24이상에서 실행하는 장치에 대해 암시적 브로드캐스트가 무시되므로 API Level24만 대상으로 지정하면 이 상태를 제거할 수 있다
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(
                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, Uri.fromFile(photoFile))
                )
            }

            // 선택한 폴더가 external media directory 경우는 불필요하지만 MediaScannerConnection을 사용하여 이미지를 스캔하지 않는 한 다른 앱이 우리의 이미지에 액세스할 수 없게 된다.
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context,
                arrayOf(photoFile.absolutePath),
                arrayOf(mimeType),
                null
            )
        }
    }

    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logd(TAG, "onViewCreated()")

        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.preview_viewfinder_camerax_f)
//        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        // 메인 액티비티로부터 받을 인텐트 필터 이벤트 설정
//        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
//        broadcastManager.registerReceiver(volumeReceiver, filter)
//        broadcastManager.registerReceiver(backReceiver, filter)

        // 장치 바향이 변경될 때마다 레이아웃을 다시 계산
        displayManager =
            viewFinder.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayManager.registerDisplayListener(displayListener, null)

        // output directory 결정
        outputDirectory = CameraXActivity.getOutputDirectory(requireContext())

        // 뷰가 제대로 배치될 때까지 기다리기
        viewFinder.post {

            // 이 뷰가 attach된 display를 계속해서 추적한다.
            displayId = viewFinder.display.displayId

            // 카메라 UI 업데이트
            updateCameraUi()

            bindCameraUseCases()
        }

        recyclerview = view.findViewById(R.id.recycler_camerax_f)
        recyclerview.layoutManager =
            LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        adapter = CameraXAdapter(view.context, onItemClickListener)
        recyclerview.adapter = adapter

        overlayImage = view.findViewById(R.id.img_overlay_camerax_f)
        closeImage = view.findViewById(R.id.img_close_camerax_f)
        opacitySeekbar = view.findViewById(R.id.seekbar_opacity_camerax_f)

        rightRotation = view.findViewById(R.id.img_right_rotation_camerax_f)
        rightRotation.setOnClickListener {

//            val matrix = Matrix()
            val matrix = overlayImage.imageMatrix
            overlayImage.scaleType = ImageView.ScaleType.MATRIX
            matrix.postRotate(90f)

            matrix.postTranslate(600f, 0f)
            overlayImage.imageMatrix = matrix

        }
//        zoomSeekbar = view.findViewById(R.id.seekbar_zoom_camerax_f)

        val scaleGestureDetector = ScaleGestureDetector(context, listener)

        viewFinder.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }


        try {
            val cursor = getImage()

            if(cursor.moveToFirst()) {
                // 1. 각 칼럼의 열 인덱스 얻기
                val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)

                // 2. 인덱스를 바탕으로 데이터를 Cursor로부터 얻기
                val id = cursor.getLong(idColNum)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                // 3. 데이터를 View로 설정
//                val inputStream = activity!!.contentResolver.openInputStream(imageUri)
//                val bitmap = BitmapFactory.decodeStream(inputStream)
//                if(inputStream != null)
//                    inputStream.close()

                Glide.with(view.context)
                    .load(imageUri)
                    .override(1080, 600)
                    .into(imgbtn_gallery_camerax_f)

//                imgbtn_gallery_camerax_f.setImageURI(imageUri)
//                imgbtn_gallery_camerax_f.setImageBitmap(bitmap)
            }
            cursor.close()
        } catch (e: SecurityException) {
            Toast.makeText(view.context, getString(R.string.toast_get_image_error_message), Toast.LENGTH_SHORT).show()
                imgbtn_gallery_camerax_f.setImageResource(R.drawable.ic_photo_library_black_24dp)
        }

        // 사진 게시글에서 카메라로 넘어올 때
        photoUrl = CameraXActivity.getPhoto()
        photoUrl?.let {
            var hasBack:Boolean

            if(it[BACK_IMAGE] != null) { // back이 있을 때
                currentPhoto = BACK_IMAGE
                hasBack = true
                Glide.with(view.context)
                    .load(it[BACK_IMAGE])
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(overlayImage)
            } else { // back이 없을 때
                currentPhoto = POST_IMAGE
                hasBack = false
                Glide.with(view.context)
                    .load(it[POST_IMAGE])
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(overlayImage)
            }

            showOverlayImage(hasBack)


        }

//        val onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            if (overlayImage.isVisible)
//                hideOverlayImage()
//            else if (recyclerview.isVisible)
//                recyclerview.visibility = View.INVISIBLE
//            else
//                activity?.onBackPressed()
//        }

        onBackPressedCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (overlayImage.isVisible)
                hideOverlayImage()
            else if (recyclerview.isVisible)
                recyclerview.visibility = View.INVISIBLE
            else
                activity?.finish()
        }
        onBackPressedCallback.isEnabled = true

        val dispatcher = requireActivity().onBackPressedDispatcher
        dispatcher.addCallback(onBackPressedCallback)

        img_changeimage_camerax_f.setOnClickListener { // 백 이미지가 있을 때만 보여짐
            // 이미지 바꾸기
            // 아이템 클릭시 아이템 내용을 기억하고 있어야 하는 변수가 필요함.
            // 그 아이템을 가지고 이미지를 바꿔주도록 하자

            if(this.photoUrl == null) return@setOnClickListener

            this.photoUrl?.let {
                if(currentPhoto == BACK_IMAGE) { // back 이면 post로 바꾸기
                    img_changeimagefront_camerax_f.visibility = View.GONE
                    img_changeimageback_camerax_f.visibility = View.VISIBLE

                    currentPhoto = POST_IMAGE
                    Glide.with(view.context)
                        .load(photoUrl?.get(POST_IMAGE))
                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .into(overlayImage)
                } else { // post면 back으로 바꾸기
//                    if(it[BACK_IMAGE] == null) { // 앞에서 처리함ㄴ
//                        Toast.makeText(view.context, getString(R.string.error_none_back_image), Toast.LENGTH_SHORT).show()
//                    } else {
                        currentPhoto = BACK_IMAGE
                        img_changeimagefront_camerax_f.visibility = View.VISIBLE
                    img_changeimageback_camerax_f.visibility = View.GONE

                        Glide.with(view.context)
                            .load(it[BACK_IMAGE])
                            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .into(overlayImage)
//                    }
                }
            }

//            if(currentPhoto == BACK_IMAGE) {
//                currentPhoto = POST_IMAGE
//                Glide.with(view.context)
//                    .load(photoUrl?.get(POST_IMAGE))
//                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
//                    .error(android.R.drawable.stat_notify_error)
//                    .into(overlayImage)
//            } else {
//                if(this.photoUrl[BACK_IMAGE] == null) {
//                    Toast.makeText(view.context, getString(R.string.error_none_back_image), Toast.LENGTH_SHORT).show()
//                } else {
//                    currentPhoto = BACK_IMAGE
//
//                    Glide.with(view.context)
//                        .load(this.photoUrl[BACK_IMAGE])
//                        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
//                        .error(android.R.drawable.stat_notify_error)
//                        .into(overlayImage)
//                }
//            }
        }

        getScrapData()
    }

    private fun getImage(): Cursor {
        val contentResolver = activity!!.contentResolver
        var queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        // 가져올 칼럼명
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.TITLE,
            MediaStore.Images.ImageColumns.DATE_TAKEN)

        // 정렬
        val sortOrder = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
        queryUri = queryUri.buildUpon().appendQueryParameter("limit", "1").build()

        return contentResolver.query(queryUri, projection, null, null, sortOrder)
    }

    val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = camera!!.cameraInfo.zoomRatio.value!! * detector.scaleFactor
            camera!!.cameraControl.setZoomRatio(scale)
//            zoomSeekbar.visibility = View.VISIBLE
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
//            zoomSeekbar.postDelayed({
//                zoomSeekbar.visibility = View.GONE
//            }, ANIMATION_ONE_SECOND)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
//            Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigate(
//                CameraXFragmentDirections.actionCameraToPermissions()
//            )
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }


    private fun updateCameraUi() {

        // 사진 캡쳐 클릭시
        view!!.findViewById<ImageButton>(R.id.imgbtn_shoot_camerax_f).setOnClickListener {
            imageCapture?.let { imageCapture ->

                view!!.findViewById<ImageButton>(R.id.imgbtn_shoot_camerax_f).
                    startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.scale_camera_shoot))

                val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

                val metadata = Metadata().apply {

                    // 전면 카메라 사용시 이미지 미러
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }

                // 사진 찍은 후 동작하는 이미지 캡쳐 리스너 설정
                imageCapture.takePicture(photoFile, metadata, mainExecutor, imageSavedListener)

                // 임시
                // 카메라 셔터소리
//                val sound = MediaActionSound()
//                sound.play(MediaActionSound.SHUTTER_CLICK)

                // API Level 23이상에서만 foreground drawable을 바꿀 수 있다.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // 사진이 캡쳐된 것을 알려주는 플래시 애니메이션 표시
                    container.postDelayed({
                        container.foreground = ColorDrawable(Color.WHITE)
                        container.postDelayed(
                            { container.foreground = null }, ANIMATION_FAST_MILLIS
                        )
                    }, ANIMATION_SLOW_MILLS)
                }
            }
        }

        // 카메라 스위치
        view!!.findViewById<ImageButton>(R.id.imgbtn_switchcamera_camerax_f).setOnClickListener {

            view!!.findViewById<ImageButton>(R.id.imgbtn_switchcamera_camerax_f).
                startAnimation(AnimationUtils.loadAnimation(context!!, R.anim.rotate_camera_switch))

            lensFacing = if (CameraSelector.LENS_FACING_FRONT === lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }

            bindCameraUseCases()
        }


        // 스크랩 사진 목록 보여주기
        view!!.findViewById<ImageButton>(R.id.imgbtn_scrap_camerax_f).setOnClickListener {
//            getScrapData()
            showScrapData()
//            showScrapData()
//            if(adapter.hasData())
//                if (recyclerview.isVisible)
//                    recyclerview.visibility = View.INVISIBLE
//                else
//                    recyclerview.visibility = View.VISIBLE
//            else
//                Toast.makeText(it.context, getString(R.string.toast_no_scrap_message), Toast.LENGTH_SHORT).show()
        }

        // Seekbar로 오버랩 이미지 투명도 설정하기
        opacitySeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                overlayImage.alpha = progress.toFloat() * 0.01f
            }
        })

        // 프리뷰위로 보여지는 것들 모두 없애기
        closeImage.setOnClickListener {
            hideOverlayImage()
        }

        // 카메라 액티비티 나가기
        view!!.findViewById<ImageView>(R.id.imgbtn_back_camerax_f).setOnClickListener {
//            activity?.onBackPressed()
            activity?.finish()

        }

        // 갤러리에서 사진 가져오기
//        view!!.findViewById<ImageButton>(R.id.imgbtn_gallery_camerax_f).setOnClickListener {
//
//            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            startActivityForResult(pickPhoto, REQUEST_CODE)
////            val intent = Intent().apply {
////                setType("image/*")
////                setAction(Intent.ACTION_GET_CONTENT)
////            }
////            startActivityForResult(intent, REQUEST_CODE)
//        }

            imgbtn_gallery_camerax_f.setOnClickListener {

//            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            startActivityForResult(pickPhoto, REQUEST_CODE)

                onBackPressedCallback.isEnabled = false

//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"))
                val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivity(intent)

//                Navigation.findNavController(requireActivity(), R.id.fragment_container_camerax).navigate(
//                    CameraXFragmentDirections.actionCameraToGallery()
//                )
        }

//        view!!.findViewById<SeekBar>(R.id.seekbar_zoom_camerax_f).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
//                camera!!.cameraControl.setLinearZoom(progress / 100.toFloat())
//            }
//            override fun onStartTrackingTouch(seekbar: SeekBar?) {}
//            override fun onStopTrackingTouch(seekbar: SeekBar?) {}
//        })
    }

    // androidx.camera.core.ImageAnalysisConfig는 androidx.camera.core.AspectRatio의 enum value가 필요하다
    // 지금은 4:3 과 16:9가 있다.
    // 제공된 값 중 하나에 대한 미리보기 비율의 절대값을 세어 파라미터로 들어온 치수에 가장 적합한 비율을 반환한다.
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    // 프리뷰, 캡쳐, analysis 선언 및 바인딩
    private fun bindCameraUseCases() {

        // 전체 화면 해상도를 위해 카메라를 설정하는데 사용되는 화면 matrics 가져오기
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        logd(TAG, "SCreen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        logd(TAG, "Preview aspect ratio: $screenAspectRatio")

        val rotation = viewFinder.display.rotation

        // CameraProvider를 LifeCycleOwner에 바인딩
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // Default PreviewSurfaceProvider
            preview?.previewSurfaceProvider = viewFinder.previewSurfaceProvider

            // ImageCapture
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // rebinding전에 use-cases를 unbind해줘야 한다
            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                loge(TAG, "Use case binding failed", e)
            }

        }, mainExecutor)

    }

    private fun showScrapData() {
        if(adapter.hasData())
            if (recyclerview.isVisible)
                recyclerview.visibility = View.INVISIBLE
            else
                recyclerview.visibility = View.VISIBLE
        else
            Toast.makeText(view!!.context, getString(R.string.toast_no_scrap_message), Toast.LENGTH_SHORT).show()
    }

    // 오버랩으로 보여주기
    private fun showOverlayImage(hasBack:Boolean) {
        overlayImage.alpha = opacitySeekbar.progress.toFloat() * 0.01f

        overlayImage.visibility = View.VISIBLE
        opacitySeekbar.visibility = View.VISIBLE
        closeImage.visibility = View.VISIBLE
        if(hasBack){
            img_changeimage_camerax_f.visibility = View.VISIBLE
            img_changeimagefront_camerax_f.visibility = View.VISIBLE
            img_changeimageback_camerax_f.visibility = View.GONE
        } else
            img_changeimage_camerax_f.visibility = View.GONE

//        rightRotation.visibility = View.VISIBLE
    }

    // 오버랩된거 숨기기
    private fun hideOverlayImage() {
        overlayImage.visibility = View.GONE
        opacitySeekbar.visibility = View.GONE
        closeImage.visibility = View.GONE
        img_changeimage_camerax_f.visibility = View.GONE
//        rightRotation.visibility = View.GONE
    }

    @SuppressLint("CheckResult")
    private fun getScrapData() {              /** temporary_token로 수정 2020-02-25 */
        Retrofit(getString(R.string.baseurl)).get(App.prefs.token, "/spott/users/my-scrap", "").subscribe({ response ->
            logd(TAG, "response: ${response.body()}")
            val result = response.body()?.let { Parser.fromJson<ArrayList<ScrapResult>>(it) }
            if(result != null) {
                var list = ArrayList<ScrapItem>()
                for(i in 0..result.size-1) {
                    list.add(ScrapItem(result[i].post.posts_image, result[i].post.back_image, result[i].post.id))
                }
                adapter.setData(list)
            }
        }, { throwable ->
            loge(TAG, throwable.message)
            if (throwable is HttpException) {
                loge(TAG, "http exception code : ${throwable.code()}, http exception message: ${throwable.message()}")
            }
        }
//            , {
//            showScrapData()
//        }
        )
    }

    companion object {

        private const val TAG = "CameraXFragment"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private const val REQUEST_CODE = 0

        // 타임스탬프된 파일을 만드는 함수
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder,
                SimpleDateFormat(
                    format,
                    Locale.KOREA
                ).format(System.currentTimeMillis()) + extension
            )
    }


    // 리사이클러뷰 어댑터 ( 자신의 스크랩 데이터 )
    inner class CameraXAdapter(val context: Context, val onItemClickListener: OnItemClickListener) :
        RecyclerView.Adapter<CameraXAdapter.ViewHolder>() {

        private var list: ArrayList<ScrapItem>

        init {
            list = ArrayList<ScrapItem>()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(context).inflate(R.layout.item_photo_square2, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = list.size

        fun setData(scraps:ArrayList<ScrapItem>) {
            list = scraps
            notifyDataSetChanged()
        }

        fun hasData():Boolean = if(list.size > 0) true else false

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context)
                .load(list[position].posts_image)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(holder.photo)

            holder.itemView.setOnClickListener {
                onItemClickListener.ItemClick(arrayOf(list[position].posts_image, list[position].back_image)) /** back_image로 수정 2020-02-25 */
            }

        }
    }
}
