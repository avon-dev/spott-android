package com.avon.spott.Login

class LoginPresenter(val loginView:LoginContract.View) : LoginContract.Presenter {

    // 프레젠터 생성시 뷰에도 프레젠터 등록하기
    init { loginView.presenter = this }

    // 이메일로그인으로 이동
    override fun openEmailLogin() { loginView.showEmailLoginUi() }

    // 회원가입으로 이동
    override fun openSignup() { loginView.showSignupUi() }
}