package com.avon.spott

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import android.widget.TextView
import java.util.regex.Matcher
import java.util.regex.Pattern




class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var tvLinkify: TextView = findViewById(R.id.text_privacyinfo_login_a)

        var text:String = "회원가입 시 Spott의 이용약관 및 개인정보 수집 이용약관에 동의하게 됩니다."

        tvLinkify.setText(text)


        val mTransform = object : Linkify.TransformFilter {
            override fun transformUrl(match: Matcher, url: String): String {
                return ""
            }
        }

        var pattern1: Pattern = Pattern.compile("이용약관")
        var pattern2: Pattern = Pattern.compile("개인정보 수집 이용약관")

        Linkify.addLinks(tvLinkify, pattern1, "http://naver.com", null, mTransform)
        Linkify.addLinks(tvLinkify, pattern2, "http://naver.com", null, mTransform)




    }
}
