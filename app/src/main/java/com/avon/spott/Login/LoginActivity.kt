package com.avon.spott.Login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Email.EmailActivity
import com.avon.spott.EmailLogin.EmailLoginActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.logd
import com.avon.spott.Utils.loge
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity(), LoginContract.View, View.OnClickListener {

    private lateinit var loginPresenter: LoginPresenter
    override lateinit var presenter: LoginContract.Presenter

    private val RC_SIGN_IN = 1
    private val TAG = "LoginActivity"


//    private lateinit var callbackManager:CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        init()
    }

    // 초기화
    private fun init() {
        loginPresenter = LoginPresenter(this)

        btn_googlelogin_login_a.setOnClickListener(this)
        btn_emaillogin_login_a.setOnClickListener(this)
        text_signup_login_a.setOnClickListener(this)
        btn_facebooklogin_a.setOnClickListener(this)

//        callbackManager = CallbackManager.Factory.create();


//        btn_facebooklogin_a.setReadPermissions("email")
//        btn_facebooklogin_a.setPermissions(Arrays.asList("email"))

//        val loginButton = findViewById(R.id.btn_facebooklogin_a) as LoginButton

        // Callback registration
//        btn_facebooklogin_a.registerCallback(callbackManager, object :
//            FacebookCallback<LoginResult?> {
//            override fun onSuccess(loginResult: LoginResult?) { // App code
//
//                if(loginResult != null) {
//                    logd(TAG, "success: ${loginResult.accessToken}")
//
//                    val request = GraphRequest.newMeRequest(loginResult.accessToken, GraphRequest.GraphJSONObjectCallback { jsonObject, response ->
//                        try {
//                            if(jsonObject.has("email")) { // 이메일을 허락해줬을 때
//                                logd(TAG, "fb email: " + jsonObject.getString("email"))
//                                Toast.makeText(this@LoginActivity, "페북 로그인 성공", Toast.LENGTH_SHORT).show()
//                            } else { // 이메일을 허락하지 않았을 때
//                                // 로그인 실패로 간주하고 권한을 허락해달라고 해야 함.
//                                LoginManager.getInstance().logOut()
////                                btn_facebooklogin_a.simulateClick()
//                                Toast.makeText(this@LoginActivity, "페북 로그인 실패 (권한이 필요합니다)", Toast.LENGTH_SHORT).show()
//                            }
//                        } catch (e:Exception) {
//                            e.printStackTrace()
//                        }
//                    })
//
//                    val parameter = Bundle()
//                    parameter.putString("fields", "id,name,email");
//                    request.parameters = parameter
//                    request.executeAsync()
//                }
//            }
//
//            override fun onCancel() { // App code
//            }
//
//            override fun onError(exception: FacebookException) { // App code
//                loge(TAG, "${exception.message}")
//                exception.printStackTrace()
//            }
//        })




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
//                Intent(this@LoginActivity, PasswordActivity::class.java).let { startActivity(it) }
//                val accessToken = AccessToken.getCurrentAccessToken()
//                val isLoggedIn = accessToken != null && !accessToken.isExpired

//                LoginManager.getInstance().logInWithReadPermissions(
//                    this@LoginActivity,
//                    Arrays.asList("email"));

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            logd(TAG, "${account.toString()}")
//            Intent(this@LoginActivity, MainActivity::class.java).let { startActivity(it) }
            Toast.makeText(this, "구글 로그인 성공", Toast.LENGTH_SHORT).show()
        } catch (e:ApiException) {
            loge(TAG, "signInResult:failed code=${e.statusCode}, ${e.message}")
            println(e.stackTrace.toString())
        }
    }

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

//            R.id.btn_facebooklogin_a -> {
//                LoginManager.getInstance().logInWithReadPermissions(this@LoginActivity, Arrays.asList("id, email"))
//
//                LoginManager.getInstance().registerCallback(callbackManager, object: FacebookCallback<LoginResult?> {
//                    override fun onSuccess(loginResult: LoginResult?) {
//                        if(loginResult != null) {
//                            logd(TAG, "success: ${loginResult.accessToken}")
//
//                            val request = GraphRequest.newMeRequest(loginResult.accessToken, GraphRequest.GraphJSONObjectCallback { jsonObject, response ->
//                                try {
//                                    if(jsonObject.has("email")) { // 이메일을 허락해줬을 때
//                                        logd(TAG, "fb email: " + jsonObject.getString("email"))
//                                        Toast.makeText(this@LoginActivity, "페북 로그인 성공", Toast.LENGTH_SHORT).show()
//                                    } else { // 이메일을 허락하지 않았을 때
//                                        // 로그인 실패로 간주하고 권한을 허락해달라고 해야 함.
//                                        LoginManager.getInstance().logOut()
//                                        Toast.makeText(this@LoginActivity, "페북 로그인 실패 (권한이 필요합니다)", Toast.LENGTH_SHORT).show()
//                                    }
//                                } catch (e:Exception) {
//                                    e.printStackTrace()
//                                }
//                            })
//
//                            val parameter = Bundle()
//                            parameter.putString("fields", "id,name,email");
//                            request.parameters = parameter
//                            request.executeAsync()
//                        }
//                    }
//
//                    override fun onCancel() {
//                    }
//
//                    override fun onError(exception: FacebookException) {
//                        loge(TAG, "${exception.message}")
//                        exception.printStackTrace()
//                    }
//                })
//
//            }
        }
    }

}
