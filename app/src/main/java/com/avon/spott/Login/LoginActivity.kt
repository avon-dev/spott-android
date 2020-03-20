package com.avon.spott.Login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.avon.spott.Data.SocialUser
import com.avon.spott.Data.Token
import com.avon.spott.Email.EmailActivity
import com.avon.spott.Email.INTENT_EXTRA_USER
import com.avon.spott.EmailLogin.EmailLoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.Nickname.NicknameActivity
import com.avon.spott.R
import com.avon.spott.TOS.TOSActivity
import com.avon.spott.Utils.App
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.loge
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


class LoginActivity : AppCompatActivity(), LoginContract.View, View.OnClickListener {

    private lateinit var loginPresenter: LoginPresenter
    override lateinit var presenter: LoginContract.Presenter

    private val RC_SIGN_IN = 1
    private val TAG = "LoginActivity"

    private val GOOGLE_USER = 9001
    private val FACEBOOK_USER = 9002

    private val SOCIAL_LOGIN = 1
    private var socialEmail:String? = null
    private var socialType:Int = GOOGLE_USER
    private lateinit var socialUser:SocialUser

    private lateinit var callbackManager:CallbackManager

    private var logined = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.avon.spott.R.layout.activity_login)

        init()
    }

    override fun onResume() {
        super.onResume()
    }

    // 초기화
    private fun init() {
//        btn_facebooklogin_a.visibility = View.GONE

        loginPresenter = LoginPresenter(this)

        btn_googlelogin_login_a.setOnClickListener(this)
        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)
//        btn_facebooklogin.setOnClickListener(this)
        btn_facebook_login_a.setOnClickListener(this)

        callbackManager = CallbackManager.Factory.create();
        btn_facebooklogin.setPermissions(Arrays.asList("email"))
        val loginButton = findViewById(R.id.btn_facebooklogin) as LoginButton

        // Callback registration
        btn_facebooklogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult?> {
            override fun onSuccess(loginResult: LoginResult?) { // App code

                if(loginResult != null) {
                    logd(TAG, "success: ${loginResult.accessToken}")

                    val request = GraphRequest.newMeRequest(loginResult.accessToken, GraphRequest.GraphJSONObjectCallback { jsonObject, response ->
                        try {
                            if(jsonObject.has("email")) { // 이메일을 허락해줬을 때
                                logd(TAG, "fb email: " + jsonObject.getString("email"))

                                socialType = FACEBOOK_USER
                                socialEmail = jsonObject.getString("email")
                                socialUser = SocialUser(socialEmail!!, socialType)
                                presenter.isPhopoUser(getString(R.string.baseurl), "/spott/social-account", socialUser)
                            } else { // 이메일을 허락하지 않았을 때
                                // 로그인 실패로 간주하고 권한을 허락해달라고 해야 함.
                                LoginManager.getInstance().logOut()
//                                btn_facebooklogin_a.simulateClick()
                                Toast.makeText(this@LoginActivity, getString(R.string.error_fb_email_permission), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    })
//
                    val parameter = Bundle()
                    parameter.putString("fields", "id,name,email");
                    request.parameters = parameter
                    request.executeAsync()
                }
            }

            override fun onCancel() { // App code
                logd(TAG, "fb cancel")
            }

            override fun onError(exception: FacebookException) { // App code
                loge(TAG, "${exception.message}")
                exception.printStackTrace()
            }
        })


        // 이용약관
        val span: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //span text 색상 변경 및 밑줄없애기, 진하게
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.span_text)
                ds.isUnderlineText = false
//                ds.setTypeface(Typeface.DEFAULT_BOLD)

            }
            override fun onClick(widget: View) {
//                Intent(this@LoginActivity, MainActivity::class.java).let { startActivity(it) }
                Intent(this@LoginActivity, TOSActivity::class.java).let { startActivity(it) }
            }
        }, 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        // 개인정보 처리 방침
        val span2: Spannable = text_privacyinfo_login_a.text as Spannable
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()

        span.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //span text 색상 변경 및 밑줄없애기, 진하게
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.span_text)
                ds.isUnderlineText = false
//                ds.setTypeface(Typeface.DEFAULT_BOLD)

            }
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, TOSActivity::class.java)
                intent.putExtra("private", "private")
                startActivity(intent)
//                val accessToken = AccessToken.getCurrentAccessToken()
//                val isLoggedIn = accessToken != null && !accessToken.isExpired

//                LoginManager.getInstance().logInWithReadPermissions(
//                    this@LoginActivity,
//                    Arrays.asList("email"));

            }
        }, 20, 28, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text_privacyinfo_login_a.movementMethod = LinkMovementMethod.getInstance()
    }

    // 메인으로 이동하기 ( 소셜 로그인 성공시 )
    override fun showMainUi(token: Token) {
        App.prefs.token = token.access
        App.prefs.refresh = token.refresh

        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // 이메일 로그인으로 이동하기
    override fun showEmailLoginUi() {
        val intent = Intent(this@LoginActivity, EmailLoginActivity::class.java)
        startActivity(intent)
    }

    // 이메일 인증으로 이동하기
    override fun showSignupUi() {
        val intent = Intent(this@LoginActivity, EmailActivity::class.java)
        startActivity(intent)
    }

    // 구글 로그인 결과
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    // 구글 로그인 결과 핸들링
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            logd(TAG, "google account: ${account.toString()}")

            socialType = GOOGLE_USER
            socialEmail = account?.email?.let { it }
            socialEmail?.let {
                socialUser = SocialUser(it, socialType)
                presenter.isPhopoUser(getString(R.string.baseurl), "/spott/social-account", socialUser)
            }
        } catch (e:ApiException) {
            loge(TAG, "signInResult:failed code=${e.statusCode}, ${e.message}")
            println(e.stackTrace.toString())
        }
    }

    override fun isPhopoUser() {
        // 가입한 유저일 때
        // 토큰 발급 받기
//        presenter.getToken()
        logd(TAG, "View.isPohopoUser() : 가입된 유저")
        socialEmail?.let {
            presenter.getToken(getString(R.string.baseurl), "/spott/token", socialUser)
        }
    }

    override fun notPhopoUser() {
        // 가입안한 유저일 때
        // 닉네임 액티비티로 이동하기
        socialEmail?.let { socialEmail ->
            Intent(this@LoginActivity, NicknameActivity::class.java).let {
                it.putExtra("login", SOCIAL_LOGIN) // 소셜 로그인에서 넘어옴
                it.putExtra(INTENT_EXTRA_USER, SocialUser(socialEmail, socialType))
                startActivity(it)
            }
        }
    }

    override fun showMessage(msgCode: Int) {
        val msg:String
        when(msgCode) {
            App.SERVER_ERROR_400 -> { msg = getString(R.string.error_400)}
            App.SERVER_ERROR_404 -> { msg = getString(R.string.error_404)}
            else -> { msg = getString(R.string.error_retry) }
        }
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    // 버튼 클릭
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_googlelogin_login_a -> { // 구글 로그인
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

                val googleSignInClient = GoogleSignIn.getClient(this, gso);

                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
            R.id.btn_emaillogin_login_a -> { // 이메일 로그인
                presenter.openEmailLogin()
            }
            R.id.text_signup_login_a -> { // 회원가입
                presenter.openSignup()
            }
            R.id.btn_facebook_login_a -> {
                btn_facebooklogin.performClick()
            }
        }
    }
}
