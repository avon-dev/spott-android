package com.avon.spott.EditMyinfo

import com.avon.spott.BaseView
import com.avon.spott.Utils.MySharedPreferences

interface EditMyInfoContract {

    interface View: BaseView<Presenter> {
        fun navigateUp() // 뒤로가기
        // 프로필 이미지 편집
        // 닉네임 수정
        // 비밀번호 변경
        // 회원 탈퇴
        fun loginActivity() // 로그아웃
    }

    interface Presenter {
        fun navigateUp() // 뒤로가기
        fun signOut(pref:MySharedPreferences) // 로그아웃
    }
}