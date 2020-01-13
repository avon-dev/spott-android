package com.avon.spott

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View

/*
TextureView를 상속받아 구성되었으며 TextureView처럼 사용됩니다.
Device의 상태변화에 따른 종횡비(aspect ratio)를 조정해주는 기능을 가집니다.

사용하는 이유
preview session만 사용하여 카메라를 통해 들어오는 정보를 출력하는 경우
카메라의 상태가 고정되어 있는 경우는 종횡비의 변화를 따로 생각할 필요가 없지만

어플리케이션에서 Orientation변화가 있고 CaptureSession을 사용하여 StillImage를 저장하는 경우 (사진을 찍고 저장하는 상황)
AR을 개발하는 경우는 종횡비의 변화에 따라 정보의 불일치가 일어날 수 있다.

ImageCapture
스마트폰, 태블릿 등 사진을 찍는 Device의 화면은 보통 가로와 세로의 크기가 다르다.
따라서 가로로 찍었을 때와 세로로 찍었을 때의 사진의 종횡비가 다르다.
따라서 현재 Device상태에 따라 다른 방식으로 사진을 저장하게 되는데
만일 디바이스의 상태와 저장시점의 상태가 달리질 경우 사진의 비율이 이상하게 저장되는 현상이 발생한다.
 */

class AutoFitTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TextureView(context, attrs, defStyle) {

    private var ratioWidth = 0
    private var ratioHeight = 0

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    fun setAspectRatio(width: Int, height: Int) {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Size cannot be negative.")
        }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }
    }

}