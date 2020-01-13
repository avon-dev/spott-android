package com.avon.spott


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.loge
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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

    private var isGallery = false

    // textureListener
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        /*
        처음 액티비티가 실행되었다 가정하면
        textureView 초기화가 완료되고 화면에 텍스쳐를 그릴준비가 되었을 때
        onSurfaceTextureAbailable()을 호출하게 됩니다.
         */
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
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

    private val stateCallback =
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                /*
                카메라가 열리고나면 onOpend(cameraDevice)가 호출됩니다
                카메라가 정상적으로 열렸으므로 이제 프리뷰세션을 만듭니다.
                 */
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
    private var imageReaader: ImageReader? = null
    private lateinit var file: File

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val onImageValiableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))
    }

    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private var state = STATE_PREVIEW
    private val cameraOpenCloseLock = Semaphore(1)
    private var flashSupported = false
    private var sensorOrientation = 0

    private val captureCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : CameraCaptureSession.CaptureCallback() {

        private fun process(result:CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
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

        private fun capturePicture(result:CaptureResult){
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.picture).setOnClickListener(this)
        view.findViewById<View>(R.id.info).setOnClickListener(this)
        textureView = view.findViewById(R.id.texture)
        textureView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
//                runPrecaptureSequence()
                captureStillPicture()
            }
            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        file = File(activity?.getExternalFilesDir(null), PIC_FILE_NAME)
    }

    override fun onResume() {
        /* 1.
        프래그먼트가 실행되면 onResume()이 호출됩니다.
        카메라와 관련된 작업은 UI를 그리는 메인쓰레드를 방해하지 않기 위해 onResume에서 새로운 쓰레드와 핸들러를 생성합니다.
        또한 Fragment에 포함된 textureView또한 인플레이팅과 동시에 초기화가 진행됩니다.

        처음 시작되면 아래 조건문에서 두번째 else조건을 타지만,
        다른 액티비티의 호출로 카메라 리소스와 텍스쳐뷰들이 잠시 비활성화 되었다가 다시 재게 되는 경우에는
        if문 안의 openCamera()를 바로 호출하게 됩니다.
         */
        super.onResume()
        startBackgroundThread()

        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        /*
        카메라 장치는 싱글톤 인스턴스이므로 사용후 반드시 시스템에 반환하여 다른 프로세스(앱)가 이용할 수 있도록 합니다.
         */
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance("카메라 권한이 필요합니다") // 처음 앱 켰을 때 취소시 에러 메세지
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setUpCameraOutputs(width: Int, height: Int) {
        /*
        후면 카메라 선택
        캡쳐된 사진(이미지리더)의 해상도, 포맷 선택
        이미지의 방향
        적합한 프리뷰 사이즈 선택
        들어오는 영상의 비율에 맞춰 TextureView의 비율 변경(이 부분은 예제에 포함된 AutoFitTextureView 커스텀 뷰입니다)
        플래시 지원 여부
         */
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                        cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                val largest = Collections.max(
                    Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea())

                imageReaader = ImageReader.newInstance(largest.width, largest.height,
                    ImageFormat.JPEG, 2).apply {
                    setOnImageAvailableListener(onImageValiableListener, backgroundHandler)
                }

                val displayRotation = requireActivity().windowManager.defaultDisplay.rotation

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                val swappedDimensions = areDimensionsSwapped(displayRotation)

                val displaySize = Point()
                requireActivity().windowManager.defaultDisplay.getSize(displaySize)
                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight,
                    maxPreviewWidth, maxPreviewHeight,
                    largest)

                if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }

                flashSupported = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                return
            }
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        } catch (e: NullPointerException) {
            ErrorDialog.newInstance("This device doesn't support Camera2 API")
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                loge(TAG, "Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    private fun openCamera(width: Int, height: Int) {
        /*
        textureView의 사이즈를 입력받아 여러가지작업들을 수행
        카메라 런타임 퍼미션이 있는지 확인
         퍼미션을 얻었다면 setUpCameraOutputs()를 수행
         */
        val permission = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
        if(permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }

        setUpCameraOutputs(width, height)
        configureTransform(width, height)

        /*
        Camera를 열기위한 선작업들이 끝났다면 CameraManager를 통해 openCamera(카메라ID, CameraDevice.StateCallback, 핸들러)를 호출
         */
        val manager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch(e: CameraAccessException) {
            loge(TAG, e.toString())
        } catch(e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReaader?.close()
            imageReaader = null
        } catch(e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start()}
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            loge(TAG, e.toString())
        }
    }

    private fun createCameraPreviewSession() {
        /*
        Preview 세션을 만들기 위해서 TextureView가 가지고 있는 SurfaceTexture를 가져옵니다.
        SurfaceTexture에 setUpCameraOutputs()에서 계산한 기본 버퍼 사이즈를 설정합니다
        SufaceTexture를 이용하여 Surface를 만듭니다.
        그런 다음 CaptureRequest.Builder에 surface를 타겟으로 지정합니다
        지정된 타겟은 실제 카메라 프레임 버퍼를 받아 처리하게 됩니다
        아직 CaptureRequest를 사용할순 없습니다. 캡쳐세션이 먼저 만들어져야합니다
        CameraDevice.createCaptureSession()을 통해 세션을 만듭니다
        캡쳐 세션이 만들어졌다면 CameraCaptureSession.StateCallback의 onConfigured()가 호출 되게 됩니다
        이곳에서 아까만든 CaptureRequest.Builder를 build하여 CaptureRequest객체를 만들고, 반복적으로 이미지 버퍼를 얻기 위해 setRepeatingRequest()를 호출합니다
        이렇게 하면 TextureView에 카메라 영상이 나오는것을 확인할 수 있습니다
         */
        try {
            val texture = textureView.surfaceTexture
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)

            previewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(Arrays.asList(surface, imageReaader?.surface),
                object: CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        captureSession = cameraCaptureSession
                        try {
                            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                            setAutoFlash(previewRequestBuilder)

                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(previewRequest,
                                captureCallback, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            loge(TAG, e.toString())
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }, null)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        /*
        스크린과 카메라의 영상의 방향을 맞추기 위해 View를 매트릭스 연산으로 회전시킴
         */
        activity ?: return

        val rotation = requireActivity().windowManager.defaultDisplay.rotation
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

    private fun lockFocus() {
        /*
        카메라에게 초점을 잡으라고 request를 보내기 위해 해당 파라미터를 설정해줍니다
        준비된 세션으로부터 capture()메소드와 함께 request인자를 넣어 호출합니다
        초점이 잡혔다면 mCaptureCallback으로부터 captureStillPicture()을 호출하게 될 것입니다
         */
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START)

            state = STATE_WAITING_LOCK
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun runPrecaptureSequence() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)

            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun captureStillPicture() {
        /*
        또 다시 CaptureSession에 CaptureRequest를 넣어 사진을 캡쳐하는데 이때의 캡쳐한 이미지 버퍼를 받을 Surface는 아까 TextureView의 Surface가 아닌 ImageReader의 Surface입니다
         */
        try {
            if ( activity == null || cameraDevice == null) return
            val rotation = requireActivity().windowManager.defaultDisplay.rotation

            val captureBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReaader?.surface)

                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }?.also { setAutoFlash(it) }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    Toast.makeText(activity, "Saved:$file", Toast.LENGTH_SHORT).show()
                    logd(TAG, file.toString())
                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder?.build(), captureCallback, null)
            }
        } catch(e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    private fun unlockFocus() {
        try {
            previewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)

            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(previewRequestBuilder.build(), captureCallback, backgroundHandler)

            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(previewRequest, captureCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            loge(TAG, e.toString())
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
//            R.id.img_overlay_camera_f -> {
//                isGallery = !isGallery
//
//                if (isGallery) {
//                    img_overlay_camera_f.visibility = View.VISIBLE
////                    frame_opacity_camera_a.visibility = visible
//                } else {
//                    img_overlay_camera_f.visibility = View.GONE
////                    frame_opacity_camera_a.visibility = gone
//                }
//            }
            R.id.picture -> lockFocus()
            R.id.info -> {
                if (activity != null) {
                    AlertDialog.Builder(activity)
                        .setMessage("This sample demonstrates the basic use of Camera2 API. Check the source code to see how\n" +
                                "you can display camera preview and take pictures.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    companion object {
        private val ORIENTATIONS = SparseIntArray()
        private val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val TAG = "CameraFragment"

        private val STATE_PREVIEW = 0 // showing camera
        private val STATE_WAITING_LOCK = 1 // waiting for the focus to be locked
        private val STATE_WAITING_PRECAPTURE = 2 // waiting for the exposure to be precapture state
        private val STATE_WAITING_NON_PRECAPTURE = 3
        private val STATE_PICTURE_TAKEN = 4
        private val MAX_PREVIEW_WIDTH = 1920
        private val MAX_PREVIEW_HEIGHT = 1080

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @JvmStatic
        private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ): Size {
            val bigEnough = ArrayList<Size>()
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width < maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w
                ) {
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
                loge(TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

        @JvmStatic
        fun newInstance(): CameraFragment = CameraFragment()
    }
}
