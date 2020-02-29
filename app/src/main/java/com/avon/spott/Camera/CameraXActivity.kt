package com.avon.spott.Camera

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.R
import com.avon.spott.Utils.logd
import java.io.File


const val KEY_EVENT_ACTION = "key_event_action"
const val KEY_EVENT_EXTRA = "key_event_extra"
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class CameraXActivity : AppCompatActivity() {

    private lateinit var container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_x)
        container = findViewById(R.id.fragment_container_camerax)

        val receive = intent.extras

        if(receive != null) {
            photoUrl = receive.getString("photoUrl")
        }
    }

    override fun onResume() {
        super.onResume()

        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        return when (keyCode) {
//            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
//                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
//                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                true
//            }
////            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_BACK -> {
////                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
////                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
////                true
////            }
//            KeyEvent.KEYCODE_BACK -> {
//                logd("LifeCycle", "keyCode - keycode_back")
//
//                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA, keyCode) }
//                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                true
//            }
//            else -> {
//                logd("LifeCycle", "keyCode: - else")
//                super.onKeyDown(keyCode, event)
//            }
//        }
//    }


    override fun onDestroy() {
        logd("LifeCycle", "CameraXActivity - onDestroy()")
        photoUrl = null
        super.onDestroy()
    }

    companion object {

        private var photoUrl:String? = null

        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, "Spott").apply { mkdirs() } }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }

        fun getPhotoURI():String? {
            return photoUrl
        }
    }

}

