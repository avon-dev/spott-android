package com.avon.spott


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.imgbtn_shoot_camera_f).setOnClickListener(this)
        view.findViewById<View>(R.id.imgbtn_gallery_camera_f).setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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

    }

    override fun onPause() {
        /*
        카메라 장치는 싱글톤 인스턴스이므로 사용후 반드시 시스템에 반환하여 다른 프로세스(앱)가 이용할 수 있도록 합니다.
         */

        super.onPause()
    }

    override fun onClick(v: View?) {

    }
}
