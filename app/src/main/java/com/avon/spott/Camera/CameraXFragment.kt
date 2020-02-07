package com.avon.spott.Camera


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.avon.spott.R
import com.avon.spott.REQUEST_CAMERA_PERMISSION
import com.avon.spott.REQUEST_STORAGE_PERMISSION
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.loge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraXFragment : Fragment() {

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var outputDirectory: File
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var displayManager: DisplayManager
    private lateinit var mainExecutor: Executor
    private lateinit var analysisExecutor: Executor

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null

    // 사진 찍을 때 쓸 볼륨 다운 버튼 리시버
    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container.findViewById<ImageButton>(R.id.imgbtn_shoot_camerax_f)
                    shutter.simulateClick()
                }
            }
        }
    }

    // 구성 변경을 유발하지 않는 방향 변경에 대해 디스플레이리스너가 필요하다.
    // 예를 들어 매니페스트에서 구성 변경 또는 180도 방향 변경에 대해 재정의하도록 선택한 경우
    private val displayListener = object: DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit // Unit 함수의 반환 구문이 없다는 것을 표현, Nothing 의미있는 데이터가 없다는 것을 명시적으로 선언하기 위해 사용
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = view?.let { view ->
            if (displayId == this@CameraXFragment.displayId) {
                logd(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.setTargetRotation(view.display.rotation)
                imageAnalyzer?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainExecutor = ContextCompat.getMainExecutor(requireContext()) // requireContext() : fragment 수명주기와 관련하여 사용해야 함.
        analysisExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()

        val cameraPermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 브로드캐스트 리시버와 리스너 해제
        broadcastManager.unregisterReceiver(volumeDownReceiver) // 동적 리시버는 해제하지 않을시 메모리 누수가 발생하므로 꼭 해준다
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_camera_x, container, false)


    private fun setGalleryThumbnail(file:File) {
        // 갤러리 미리보기를 고정하는 뷰
        val thumbnail = container.findViewById<ImageButton>(R.id.imgbtn_gallery_camerax_f)

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
    private val imageSavedListener = object : ImageCapture.OnImageSavedCallback {
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
            MediaScannerConnection.scanFile(context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
        }
    }







    private fun requestCameraPermission() {
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }

        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
        }
    }

    companion object {

        private const val TAG = "CameraXFragment"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        // 타임스탬프된 파일을 만드는 함수
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(baseFolder, SimpleDateFormat(format, Locale.KOREA).format(System.currentTimeMillis()) + extension)
    }
}
