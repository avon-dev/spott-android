package com.avon.spott.EmailLogin

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.Data.Token
import com.avon.spott.FindPW.FindPWActivity
import com.avon.spott.Main.MainActivity
import com.avon.spott.R
import com.avon.spott.Utils.logd
import kotlinx.android.synthetic.main.activity_email_login.*
import kotlinx.android.synthetic.main.toolbar.*
import java.security.PublicKey
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


class EmailLoginActivity : AppCompatActivity(), EmailLoginContract.View, View.OnClickListener {

    override lateinit var presenter: EmailLoginContract.Presenter
    private lateinit var emailLoginPresenter: EmailLoginPresenter

    private val TAG = "EmailLoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_login)

        init()
    }

    private fun init() {

        // 임시
//        edit_username_emaillogin_a.setText("pms939@test.com")
//        edit_password_emaillogin_a.setText("qwer1234!")

//        edit_username_emaillogin_a.setText("baek@seunghyun.com")
//        edit_password_emaillogin_a.setText("seunghyun1!")


        emailLoginPresenter = EmailLoginPresenter(this)

        text_title_toolbar.text = getString(R.string.text_title_emaillogin)

        img_back_toolbar.setOnClickListener(this)
        text_findpw_emaillogin_a.setOnClickListener(this)
        btn_login_emaillogin_a.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun showMainUi(token: Token) {
        val pref = getSharedPreferences("pref", Context.MODE_PRIVATE)
        val ed = pref.edit()
        ed.putString("access", token.access)
        ed.putString("refresh", token.refresh)
        ed.apply()

        Intent(applicationContext, MainActivity::class.java).let {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    override fun showFindPWUi() {
        val intent = Intent(this@EmailLoginActivity, FindPWActivity::class.java)
        startActivity(intent)
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun showError(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_back_toolbar -> {
                presenter.navigateUp()
//                getAsyncTask().execute()
            }

            R.id.text_findpw_emaillogin_a -> {
                presenter.openFindPW()
            }

            R.id.btn_login_emaillogin_a -> { // 로그인 버튼

                if (edit_username_emaillogin_a.text.toString().equals("")) {
                    showError("이메일을 입력해주세요")
                    return
                }

                if (edit_password_emaillogin_a.text.toString().equals("")) {
                    showError("비밀번호를 입력해주세요")
                    return
                }

                presenter.signIn(
                    getString(R.string.baseurl),
                    edit_username_emaillogin_a.text.toString(),
                    edit_password_emaillogin_a.text.toString()
                )
            }
        }
    }


    inner class getAsyncTask() : AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit?) {
            val hostname = "https://wikipedia.org"
            val factory:SSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
            val socket:SSLSocket = factory.createSocket(hostname, 443) as SSLSocket
            socket.startHandshake()

            var certs = socket.session.peerCertificates
            val cert = certs[0]
            val key:PublicKey = cert.publicKey

            logd(TAG, "다다다ㅏ다다다" + key.toString())
        }
    }
    private fun ssltest() {

        val th = Thread {
//            val url = URL("https://wikipedia.org")
//            val url = URL("http://api.phopo.best")
//            val urlConnection:URLConnection = url.openConnection()
//            var inputStream:InputStream
//            inputStream = urlConnection.getInputStream()

            val hostname = "https://wikipedia.org"
            val factory:SSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory()
            val socket:SSLSocket = factory.createSocket(hostname, 443) as SSLSocket
            socket.startHandshake()

            var certs = socket.session.peerCertificates
            val cert = certs[0]
            val key:PublicKey = cert.publicKey


            val a = 10
            val b = a

        }

        th.start()

    }

}
