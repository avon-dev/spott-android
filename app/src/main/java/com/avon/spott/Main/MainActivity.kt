package com.avon.spott.Main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.avon.spott.Camera.CameraActivity
import com.avon.spott.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.view.*

class MainActivity : AppCompatActivity(), MainContract.View, View.OnClickListener {

    private lateinit var mainPresenter: MainPresenter
    override lateinit var presenter: MainContract.Presenter

    companion object{
        lateinit var mToolbar : ConstraintLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mToolbar = this.findViewById<ConstraintLayout>(R.id.include_toolbar)

        if(savedInstanceState ==null){
            setupBottomNavigationBar()
        }

        init()

    }

    fun init(){
        // 프레젠터 생성
        mainPresenter = MainPresenter(this)

        // 버튼 클릭 리스너
        mToolbar.img_back_toolbar.setOnClickListener(this)
        const_camera_main_a.setOnClickListener(this)
    }

    override fun showCameraUi(){
        startActivity(Intent(this, CameraActivity::class.java))
    }

    override fun navigateUp() {
        onBackPressed()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomnav_main_a)

        val navGraphIds = listOf(R.navigation.home, R.navigation.map, R.navigation.scrap, R.navigation.mypage)

        // Setup the bottom navigation view with a list of navigation graphs
        bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.frag_navhost_main_a,
            intent = intent
        )

    }

    override fun onClick(v: View?){
        when(v?.id){
            R.id.const_camera_main_a -> {presenter.openCamera()}
            R.id.img_back_toolbar -> {presenter.navigateUp()}
        }
    }




}
