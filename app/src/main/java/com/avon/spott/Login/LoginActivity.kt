package com.avon.spott.Login

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.Spanned
import android.text.format.DateFormat
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Email.EmailActivity
import com.avon.spott.EmailLogin.EmailLoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.Password.PasswordActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity(), LoginContract.View, View.OnClickListener {

    private lateinit var loginPresenter: LoginPresenter
    override lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()

        try {
            val cursor:Cursor = getImage()
            if (cursor.moveToFirst()) {
                // 1. 각 칼럼의 열 인덱스 얻기
                val idColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
                val titleColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.TITLE)
                val dateTakenColNum = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)

                // 2. 인덱스를 바탕으로 데이터를 Cursor로부터 얻기
                val id = cursor.getLong(idColNum)
                val title = cursor.getString(titleColNum)
                val dateTaken = cursor.getLong(dateTakenColNum)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                // 3. 데이터를 View로 설정
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateTaken
                val text = DateFormat.format("yyyy/MM/dd(E) kk:mm:ss", calendar).toString()
                test_img.setImageURI(imageUri)
            }
            cursor.close()
        } catch (e: SecurityException) {
            Toast.makeText(this, "스토리지에 접근 권한을 허가로 해주세요.（종료합니다)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 초기화
    private fun init() {
        loginPresenter = LoginPresenter(this)

        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)

        val span: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, MainActivity::class.java).let { startActivity(it) }
            }
        }, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        val span2: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Intent(this@LoginActivity, PasswordActivity::class.java).let { startActivity(it) }
            }
        }, 20, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun showEmailLoginUi() {
        val intent = Intent(this@LoginActivity, EmailLoginActivity::class.java)
        startActivity(intent)
    }

    override fun showSignupUi() {
        val intent = Intent(this@LoginActivity, EmailActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_emaillogin_login_a -> {
                presenter.openEmailLogin()
            }
            R.id.text_signup_login_a -> {
                presenter.openSignup()
            }
        }
    }


    /*
    TEST
     */



    private fun getImage(): Cursor {
        val contentResolver = contentResolver
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
}
