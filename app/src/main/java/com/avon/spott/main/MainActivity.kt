package com.avon.spott.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.ui.setupWithNavController
import com.avon.spott.CameraActivity
import com.avon.spott.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.view.*

class MainActivity : AppCompatActivity() {

    companion object{
        lateinit var mToolbar : ConstraintLayout
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState ==null){
            setupBottomNavigationBar()
        }

        mToolbar = this.findViewById<ConstraintLayout>(
            R.id.include_toolbar
        )

        const_camera_main_a.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        mToolbar.img_back_toolbar.setOnClickListener {
            onBackPressed()
        }


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




}
