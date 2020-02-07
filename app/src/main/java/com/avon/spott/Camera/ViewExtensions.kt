package com.avon.spott.Camera

import android.view.View
import android.widget.ImageButton

const val FLAGS_FULLSCREEN =
    View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

// 밀리세컨즈 UI 애니메이션
const val ANIMATION_FAST_MILLIS = 50L
const val ANIMATION_SLOW_MILLS = 100L

// 애니메이션을 트리거하기 위해 버튼을 누르는 동안 약간의 지연을 포함하여 버튼 클릭을 시뮬레이션한다.
fun ImageButton.simulateClick(delay: Long = ANIMATION_FAST_MILLIS) {
    performClick() // 뷰를 클릭한 것과 같은 효과
    isPressed = true
    postDelayed({
        invalidate() // 애니메이션이후 뷰를 무효화시켜 화면을 다시 그리기 위해 onDraw를 호출하도록 한다.
        isPressed = false
    }, delay)
}