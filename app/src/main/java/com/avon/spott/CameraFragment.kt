package com.avon.spott


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avon.spott.Utils.loge
import com.bumptech.glide.Glide
import java.io.File
import java.util.*
import java.util.Arrays.asList
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


/**
 * CameraManager : 시스템 서비스, 사용 간으한 카메라와 카메라 기능들을 쿼리할 수 있고 카메라를 열 수 있습니다.
 * CameraCharacteristics : 카메라의 속성들을 담고 있는 객체
 * CameraDevice : 카메라 객체
 * CaptureRequest : 사진 촬영이나 카메라 미리보기를 요청(request)하는데 쓰이는 객체
 *                  카메라의 설정을 변경할 때도 관여합니다.
 * CameraCaptureSession : CaptureRequest를 보내고 카메라 하드웨어에서 결과를 받을 수 있습니다.
 * CaptureResult : CaptureRequest의 결과물입니다.
 *                 이미지의 메타데이터도 가져올 수 있습니다.
 */
class CameraFragment : Fragment(), View.OnClickListener,
    ActivityCompat.OnRequestPermissionsResultCallback {

    private val surfaceTextureListener = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit
    }

    private lateinit var cameraId: String
    private lateinit var textureView: AutoFitTextureView
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size

    private val stateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraFragment.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }
        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraFragment.cameraDevice = null
        }
        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@CameraFragment.activity?.finish()
        }
    }

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var imageReader: ImageReader? = null
    private lateinit var file: File

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))

//        val wallpaper_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "spott" + File.separator
//        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
//            val file = File(wallpaper_url)
//        }
    }

    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private var state = STATE_PREVIEW

    private val cameraOpenCloseLock = Semaphore(1)

    private var flashSupported = false

    private var sensorOrientation = 0

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState === CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                    afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }
    }

    private var swap:Boolean = false
    private lateinit var seekbar: SeekBar
    private lateinit var overlay: ImageView
    private lateinit var recycler: RecyclerView

    interface ClickListener { fun Click(uri:String) }
    private val clickListener = object: ClickListener {
        override fun Click(uri:String) {

            if(overlay.isVisible) {
                    overlay.visibility = View.GONE
                    seekbar.visibility = View.GONE
            }
            else {
                Glide.with(view!!.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(overlay)

                overlay.alpha = seekbar.progress.toFloat() * 0.01f

                overlay.visibility = View.VISIBLE
                seekbar.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.imgbtn_shoot_camera_f).setOnClickListener(this)
        view.findViewById<View>(R.id.imgbtn_gallery_camera_f).setOnClickListener(this)
        view.findViewById<View>(R.id.imgbtn_back_camera_f).setOnClickListener(this)
        view.findViewById<View>(R.id.imgbtn_swap_camera_f).setOnClickListener(this)
        view.findViewById<View>(R.id.imgbtn_scrap_camera_f).setOnClickListener(this)
        textureView = view.findViewById(R.id.texture_camera_f)
        overlay = view.findViewById<ImageView>(R.id.img_overlay_camera_f)

        seekbar = view.findViewById<SeekBar>(R.id.seekbar_opacity_camera_f)
        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                overlay.alpha = progress.toFloat() * 0.01f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        recycler = view.findViewById(R.id.recycler_camera_f)
        recycler.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        recycler.adapter = CameraAdapter(view.context, clickListener)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        val wallpaper_url = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "spott" + File.separator
//        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
//            file = File(wallpaper_url)
//        }

//        file = File(activity!!.getExternalFilesDir(null), PIC_FILE_NAME)
//        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), PIC_FILE_NAME)
//        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath, PIC_FILE_NAME)
        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator + "Camera", PIC_FILE_NAME)

//        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString(), PIC_FILE_NAME)
//        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "spott", PIC_FILE_NAME)
//        file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "spott" + File.separator, PIC_FILE_NAME)


    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }


    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch ( e: InterruptedException) {
            loge(TAG, e.toString())
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture

            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)

            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(Arrays.asList(surface, imageReader?.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                    if (cameraDevice == null) return

                    captureSession = cameraCaptureSession
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                        previewRequest = previewRequestBuilder.build()
                        captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
                    } catch (e: CameraAccessException) {
                        loge(TAG, e.toString())
                    }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, null)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun openCamera(width: Int, height: Int) {
        val cameraPermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }

//        val storagePermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        if (storagePermission  != PackageManager.PERMISSION_GRANTED) {
//            requestStoragePermission()
//            return
//        }

        setUpCameraOutputs(width, height) // 카메라와 프리뷰 설정
        configureTransform(width, height) // 화면 회전 설정

        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            if ( !cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted whilte trying to lock camera opening.", e)
        }

    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        activity ?: return

        val rotation = activity!!.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                viewHeight.toFloat() / previewSize.height,
                viewWidth.toFloat() / previewSize.width)

            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation -2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    // Permission [Start]
    private fun requestCameraPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == REQUEST_CAMERA_PERMISSION) {
            if(grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance("권한을 확인해주세요").show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
    // Permission [End]

    private fun setUpCameraOutputs(width: Int, height: Int) {
        val a = arrayOf(1, 2, 3)
        val list = asList(-1, 0, *a, 4)

        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)

                if(!swap) {
                    if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                        continue
                    }
                } else {
                    if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                        continue
                    }
                }

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                val largest = Collections.max(Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())

                imageReader = ImageReader.newInstance(largest.width, largest.height, ImageFormat.JPEG, 2).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                val displayRotation = activity!!.windowManager.defaultDisplay.rotation
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)

                val swappedDimensions = areDimensSwapped(displayRotation)

                val displaySize = Point()
                activity!!.windowManager.defaultDisplay.getSize(displaySize)

                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest)

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }

                if(!swap) {
                    this.cameraId = cameraId
                } else {
                    swap = false
                }
                return
            }
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        } catch (e: NullPointerException) {
            ErrorDialog.newInstance("이 기기는 Camera2 API를 지원하지 않습니다").show(childFragmentManager, FRAGMENT_DIALOG)
        }

//        getBackFacingCameraId(manager)
    }

    private fun areDimensSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180){
                    swappedDimensions = true
                }
            }
            else -> {
                loge(TAG, "Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    private fun runPrecaptureSequence() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun captureStillPicture() {
        try {
            if (activity == null || cameraDevice == null) {
                return
            }

            val rotation = activity!!.windowManager.defaultDisplay.rotation

            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader?.surface)
                set(CaptureRequest.JPEG_ORIENTATION, (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }

            val captureCallback = object: CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    Toast.makeText(activity, "Saved:$file",Toast.LENGTH_SHORT).show()
                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun unlockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun lockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            state = STATE_WAITING_LOCK
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    override fun onClick(v: View) {
        when(v.id) {
            R.id.imgbtn_back_camera_f -> { activity?.onBackPressed() }
            R.id.imgbtn_swap_camera_f -> { // 전면, 후면 카메라 바꾸기 ( 근데 요즘엔 후면 카메라가 여러개 일 때가 있다 ) - 기본 카메라 앱에는 그래서 후면 카메라 선택아이콘이 있기는 함
               switchCamera()
            }
            R.id.imgbtn_shoot_camera_f -> {
                lockFocus()
//                Toast.makeText(context, "w: ${textureView.width}, h: ${textureView.height}", Toast.LENGTH_SHORT).show()
            }
            R.id.imgbtn_scrap_camera_f -> {
                if(recycler.isVisible)
                    recycler.visibility = View.INVISIBLE
                else
                    recycler.visibility = View.VISIBLE
            }
        }
    }

    private fun switchCamera() {
        if(cameraId.equals("0")) { // 전면으로 바꾸기
            swap = true
            cameraId = "1"
            closeCamera()
            reopenCamera()
        } else if (cameraId.equals("1")) { // 후면으로 바꾸기
            swap = false
            cameraId = "0"
            closeCamera()
            reopenCamera()
        }
    }

    private fun reopenCamera() {
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    companion object {

        private val TAG = "CameraFragment"
        private val FRAGMENT_DIALOG = "dialog"

        private val ORIENTATIONS = SparseIntArray()

        private val STATE_PREVIEW = 0
        private val STATE_WAITING_LOCK = 1
        private val STATE_WAITING_PRECAPTURE = 2
        private val STATE_WAITING_NON_PRECAPTURE = 3
        private val STATE_PICTURE_TAKEN = 4

        private val MAX_PREVIEW_WIDTH = 1920
        private val MAX_PREVIEW_HEIGHT = 1080

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        @JvmStatic
        private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ) : Size {
            val bigEnough = ArrayList<Size>()
            val notBigEnough = ArrayList<Size>()

            val w = aspectRatio.width
            val h = aspectRatio.height

            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight
                    && option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                return Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                loge(TAG, "Couldent find any suitable preview size")
                return choices[0]
            }

        }

        @JvmStatic
        fun newInstance(): CameraFragment = CameraFragment()
    }

    inner class CameraAdapter(val context:Context, val clickListener:ClickListener): RecyclerView.Adapter<CameraAdapter.ViewHolder>() {



        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_photo_square2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == 0 || position == 5) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/06/12/06/people-2591874_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 1 || position == 6) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/06/23/17/41/morocco-2435391_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)

            } else if (position == 2 || position == 7) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2012/10/10/11/05/space-station-60615_960_720.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 3 || position == 8) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2017/08/02/00/16/people-2568954_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            } else if (position == 4 || position == 9) {
                Glide.with(holder.itemView.context)
                    .load("https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg")
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.photo)
            }

            holder.itemView.setOnClickListener {
                clickListener.Click("https://cdn.pixabay.com/photo/2016/11/29/06/45/beach-1867881_1280.jpg")
            }

        }
        override fun getItemCount(): Int = 10

        inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val photo = itemView.findViewById<ImageView>(R.id.img_photo_photo_square_i2) as ImageView
        }
    }
}
