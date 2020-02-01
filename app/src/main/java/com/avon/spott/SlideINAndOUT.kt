package com.avon.spott

import android.content.Context
import android.view.View
import android.view.animation.Animation
import com.avon.spott.Utils.logd

fun animSlide(context: Context, view : View, show : Boolean){ //버튼 나오고 사라지는 애니메이션
    val animationFadeOut = android.view.animation.AnimationUtils.loadAnimation(context,
        if(show) R.anim.translate_left else R.anim.translate_right) //버튼 나오거나 사라지는 애니메이션
    animationFadeOut.setAnimationListener(object : Animation.AnimationListener{
        override fun onAnimationRepeat(animation: Animation?) {

        }
        override fun onAnimationEnd(animation: Animation?) { //애니메이션 끝났을때

        }
        override fun onAnimationStart(animation: Animation?) {
            view.visibility = if(show) View.VISIBLE else View.GONE  // show값에 따른 visible 처리
        }
    })
    view.startAnimation(animationFadeOut)
}