package com.avon.spott.Camera

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.avon.spott.R
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    // All of temp code
    private val visible = View.VISIBLE
    private val invisible = View.INVISIBLE
    private val gone = View.GONE

    private lateinit var translateLeftAnim: Animation
    private lateinit var translateRightAnim: Animation
    private var isOpen = false

    private var isGallery = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        imgbtn_gallery_camera_a.setOnClickListener(this)
        imgbtn_like_camera_a.setOnClickListener(this)

        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left)
        translateRightAnim = AnimationUtils.loadAnimation(this,
            R.anim.translate_right
        )

        translateLeftAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                if (isOpen) {
                    recycler_camera_a.visibility = invisible
                    isOpen = false
                } else {
                    isOpen = true
                }
            }
        })
        translateRightAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                if (isOpen) {
                    recycler_camera_a.visibility = invisible
                    isOpen = false
                } else {
                    isOpen = true
                }
            }
        })

        seekbar_opacity_camera_a.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                img_overlay_camera_a.alpha = progress.toFloat() * 0.01f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun gallery() {
        isGallery = !isGallery

        if (isGallery) {
            img_overlay_camera_a.visibility = visible
            frame_opacity_camera_a.visibility = visible
        } else {
            img_overlay_camera_a.visibility = gone
            frame_opacity_camera_a.visibility = gone
        }
    }

    fun recycler() {
        if (isOpen) {
            recycler_camera_a.startAnimation(translateRightAnim)
            recycler_camera_a.visibility = gone
        } else {
            recycler_camera_a.startAnimation(translateLeftAnim)
            recycler_camera_a.visibility = visible
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgbtn_gallery_camera_a -> {
                if (isOpen)
                    recycler()
                gallery()
            }

            R.id.imgbtn_like_camera_a -> {
                if (isGallery)
                    gallery()
                recycler()
            }
        }
    }
}
