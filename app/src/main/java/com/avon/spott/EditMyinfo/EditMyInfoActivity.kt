package com.avon.spott.EditMyinfo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.avon.spott.Camera.PermissionsFragment.Companion.hasPermissions
import com.avon.spott.ChangePassword.ChangePasswordActivity
import com.avon.spott.Data.UserInfo
import com.avon.spott.Login.LoginActivity
import com.avon.spott.Mypage.MypageFragment.Companion.userDataChange
import com.avon.spott.R
import com.avon.spott.Utils.App
import com.avon.spott.Utils.MySharedPreferences
import com.avon.spott.Utils.logd
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.activity_edit_my_info.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

class EditMyInfoActivity : AppCompatActivity(), EditMyInfoContract.View, View.OnClickListener {

    private val TAG = "EditMyInfoActivity"

    private val CROPPED_PROFILE_IMAGE_NAME = "ProfileCropImage.jpg"

    private val CHANGE_PROFILE_IMAGE = 0
    private val DELETE_PROFILE_IMAGE = 1

    private val ERROR_DUPLICATION_NICKNAME = 3
    private val SUCCESS_CHANGE_NICKNAME = 4
    private val ERROR_INVALID_NICKNAME = 5
    private val ERROR_RETRY = 999

    private val SELECT_IMAGE = 102

    override lateinit var presenter: EditMyInfoContract.Presenter
    private lateinit var editmyinfoPresenter: EditMyInfoPresenter

    private var editable:Boolean = false
    private lateinit var buffNickname: String
    private var validNickname:Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_info)

        init()
    }


    private fun init() {
        editmyinfoPresenter = EditMyInfoPresenter(this)

        text_title_toolbar.setText(getString(R.string.title_editmyinfo))

        img_back_toolbar.setOnClickListener(this)
        img_profile_editmyinfo_a.setOnClickListener(this)
        imgbtn_editnickname_editmyinfo_a.setOnClickListener(this)
        btn_changepw_editmyinfo_a.setOnClickListener(this)
        btn_withdrawal_editmyinfo_a.setOnClickListener(this)
        btn_signout_editmyinfo_a.setOnClickListener(this)

        val access = MySharedPreferences(applicationContext).prefs.getString("access", "")

        if (access != null) // Shared에 토큰값이 있을 때
            presenter.getUser(getString(R.string.baseurl), access)

        edit_nickname_editmyinfo_a.addTextChangedListener {
            presenter.isNickname(edit_nickname_editmyinfo_a.text.toString())
        }
    }
    override fun getUserInfo(userInfo: UserInfo) {
        buffNickname = userInfo.nickname.toString()

        edit_nickname_editmyinfo_a.setText(userInfo.nickname)
        if(userInfo.profile_image != null) { // 프로필 이미지 있으면 이미지 세팅하기
            Glide.with(this@EditMyInfoActivity)
                .load(userInfo.profile_image)
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .error(android.R.drawable.stat_notify_error)
                .into(img_profile_editmyinfo_a)
        }

        if(userInfo.user_type == 9000) {
            btn_email_editmyinfo_a.setText(userInfo.email)
        } else {
            btn_email_editmyinfo_a.visibility = View.GONE
            btn_changepw_editmyinfo_a.visibility = View.GONE
        }
    }


    override fun navigateUp() {
        onBackPressed()
    }

    override fun validNickname(valid: Boolean) {
        validNickname = valid
    }

    override fun changedProfile(result:Boolean, photoUri: Uri) {
        if(result){
            img_profile_editmyinfo_a.setImageURI(photoUri)
            userDataChange = true
        } else {
            showMessage(ERROR_RETRY)
        }
    }

    override fun getNickname(result: Boolean) {
        if(!result) { // 닉네임 변경에 실패 했을 때
            edit_nickname_editmyinfo_a.setText(buffNickname) // 이전 닉네임으로 되돌리기
            showMessage(ERROR_DUPLICATION_NICKNAME)
        } else {
            buffNickname = edit_nickname_editmyinfo_a.text.toString()
            userDataChange = true
            showMessage(SUCCESS_CHANGE_NICKNAME)
        }

    }

    override fun withDrawl(result: Boolean) {
        if(result) {
            presenter.signOut(MySharedPreferences(this))
        } else {
            showMessage(ERROR_RETRY)
        }
    }

    override fun loginActivity() { // 로그아웃
        val intent = Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun deleteProfileImage() {
        img_profile_editmyinfo_a.setImageResource(R.drawable.img_person)
    }

    override fun showMessage(code: Int) {
        when(code) {
            App.SERVER_ERROR_400 -> {
                Toast.makeText(applicationContext, getString(R.string.error_400), Toast.LENGTH_SHORT).show()
            }
            App.SERVER_ERROR_404 -> {
                Toast.makeText(applicationContext, getString(R.string.error_404), Toast.LENGTH_SHORT).show()
            }
            App.SERVER_ERROR_500 -> {
                Toast.makeText(applicationContext, getString(R.string.error_500), Toast.LENGTH_SHORT).show()
            }
            ERROR_DUPLICATION_NICKNAME -> {
                Toast.makeText(applicationContext, getString(R.string.error_duplication_nickname), Toast.LENGTH_SHORT).show()
            }
            ERROR_INVALID_NICKNAME -> {
                Toast.makeText(applicationContext, getString(R.string.error_invalid_nickname), Toast.LENGTH_SHORT).show()
            }
            SUCCESS_CHANGE_NICKNAME -> {
                Toast.makeText(applicationContext, getString(R.string.success_changed_nickname), Toast.LENGTH_SHORT).show()
            }
            ERROR_RETRY -> {
                Toast.makeText(applicationContext, getString(R.string.error_retry), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) { // 권한이 있으면

                val builder = AlertDialog.Builder(this)

                builder.setTitle("프로필 이미지 수정")
                    .setItems(R.array.edit_profile_item, DialogInterface.OnClickListener { _, pos ->
                        when(pos) {
                            CHANGE_PROFILE_IMAGE -> {
                                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                startActivityForResult(pickPhoto, SELECT_IMAGE)
                            }
                            DELETE_PROFILE_IMAGE -> {
                                presenter.deleteProfileImage(getString(R.string.baseurl), App.prefs.token, "/spott/users")
                            }
                        }
                    })

                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && null != data) {
            if(requestCode == SELECT_IMAGE) {
                if (data.getData() != null) {
                    var mPhotoPath: Uri = data.getData() as Uri
                    logd(TAG, "photopath : " + mPhotoPath)

                    val options = UCrop.Options()
                    options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.SCALE)
                    options.setToolbarTitle("")
                    options.setToolbarCropDrawable(R.drawable.ic_arrow_forward_black_24dp)
                    options.setActiveControlsWidgetColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                    options.setStatusBarColor(ContextCompat.getColor(applicationContext, R.color.bg_black))
                    options.setAspectRatioOptions(0,
                        AspectRatio("1 : 1", 1f, 1f)
                    )
                    options.setCircleDimmedLayer(true)

                    /* 현재시간을 임시 파일 이름에 넣는 이유 : 중복방지
                    / (안넣으면 AddPhotoActivity의 이미지뷰에 다른 사진 보여진다.) */
                    val timeStamp = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
                    UCrop.of(mPhotoPath, Uri.fromFile(File(applicationContext.cacheDir, timeStamp+CROPPED_PROFILE_IMAGE_NAME)))
                        .withMaxResultSize(resources.getDimension(R.dimen.upload_width).toInt(),
                            resources.getDimension(R.dimen.upload_heigth).toInt())
                        .withOptions(options)
                        .start(this)
                }
            } else if(requestCode == UCrop.REQUEST_CROP){
                var mCropPath: Uri? = UCrop.getOutput(data)
                logd(TAG, "croppath : " + mCropPath)

                val token = MySharedPreferences(applicationContext).prefs.getString("access", "")
                if(token != null && mCropPath != null) {
                    presenter.setProfileImage(getString(R.string.baseurl), token, mCropPath)
                }
            }
        }
        if(resultCode == UCrop.RESULT_ERROR){
            logd(TAG, "error : Ucrop result error")
        }
    }

    @SuppressLint("ResourceType")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.img_back_toolbar -> { // 뒤로가기
                presenter.navigateUp()
            }
            R.id.img_profile_editmyinfo_a -> { // 프로필 이미지 편집
                if(!hasPermissions(applicationContext)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
                    }
                } else { // 프로필 이미지 수정 전 다이얼로그 띄우기
                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("프로필 이미지 수정")
                        .setItems(R.array.edit_profile_item, DialogInterface.OnClickListener { _, pos ->
                            when(pos) {
                                CHANGE_PROFILE_IMAGE -> {
                                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                    pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    startActivityForResult(pickPhoto, 102)
                                }
                                DELETE_PROFILE_IMAGE -> {
                                    presenter.deleteProfileImage(getString(R.string.baseurl), App.prefs.token, "/spott/users")
                                }
                            }
                        })

                    val dialog = builder.create()
                    dialog.show()
                }
            }

            R.id.imgbtn_editnickname_editmyinfo_a -> { // 닉네임 수정
                editable = !editable
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                if(editable) { // 닉네임 변경하기
                    imgbtn_editnickname_editmyinfo_a.setImageResource(R.drawable.ic_done_black_24dp)

                    edit_nickname_editmyinfo_a.apply { // 수정 가능한 상태로 만들기
                        isFocusableInTouchMode = true
                        isFocusable = true
                        setBackgroundColor(Color.LTGRAY)
                        setSelection(edit_nickname_editmyinfo_a.length())
                    }

                    imm.showSoftInput(edit_nickname_editmyinfo_a, 0)
                }else { // 닉네임 변경 완료
                    imgbtn_editnickname_editmyinfo_a.setImageResource(R.drawable.baseline_edit_24)

                    edit_nickname_editmyinfo_a.apply { // 수정 불가능한 상태로 만들기
                        isClickable = false
                        isFocusable = false
                        setBackgroundColor(Color.TRANSPARENT)
                    }

                    imm.hideSoftInputFromWindow(edit_nickname_editmyinfo_a.windowToken, 0)

                    if(validNickname) { // 닉네임을 규칙에 맞게 작성했을 때에만 서버에 요청을 날린다.
                        val token =
                            MySharedPreferences(applicationContext).prefs.getString("access", "")

                        if (token != null && !buffNickname.equals(edit_nickname_editmyinfo_a.text.toString()))
                            presenter.changeNickname(getString(R.string.baseurl), token, edit_nickname_editmyinfo_a.text.toString())
//                        else
//                            Toast.makeText(applicationContext, "닉네임을 변경해주세요", Toast.LENGTH_SHORT).show()

                    } else { // 닉네임이 규칙에 맞지 않은경우
                        edit_nickname_editmyinfo_a.setText(buffNickname)
                        Toast.makeText(applicationContext, getString(R.string.hint_nickname), Toast.LENGTH_SHORT).show()
                    }
                }

            }

            R.id.btn_changepw_editmyinfo_a -> { // 비밀번호 변경
                // 현재 비밀번호 입력 후 새 비밀번호 입력 창을 띄운다
                Intent(applicationContext, ChangePasswordActivity::class.java).let{
                    startActivity(it)
                }
            }

            R.id.btn_withdrawal_editmyinfo_a -> { // 회원 탈퇴

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@EditMyInfoActivity)
                builder.setMessage(R.string.dialog_withdrawl_message)
                    .setTitle(R.string.dialog_withdrawl_title)
                    .setNegativeButton(R.string.cancel, DialogInterface.OnClickListener{ dialog, _ ->
                        dialog.cancel()
                    })
                    .setPositiveButton(R.string.ok, DialogInterface.OnClickListener{ _, _->
                        // 서버와 통신해서 데이터 없애고 로그아웃 로직
                        val token = MySharedPreferences(applicationContext).prefs.getString("access", "")
                        presenter.withDrawl(getString(R.string.baseurl), token!!)
                    })

                val dialog:AlertDialog = builder.create()
                dialog.show()

//                // 서버와 통신해서 데이터 없애고 로그아웃 로직
//                val token = MySharedPreferences(applicationContext).prefs.getString("access", "")
//                presenter.withDrawl(getString(R.string.baseurl), token)
            }

            R.id.btn_signout_editmyinfo_a -> { // 로그아웃
                presenter.signOut(MySharedPreferences(this))
            }
        }
    }
}
