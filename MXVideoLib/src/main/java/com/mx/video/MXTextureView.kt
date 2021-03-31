package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

class MXTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    private var currentVideoWidth = 0
    private var currentVideoHeight = 0
    private var displayType = MXVideoDisplay.CENTER_CROP

    fun setVideoSize(currentVideoWidth: Int, currentVideoHeight: Int) {
        if (this.currentVideoWidth != currentVideoWidth || this.currentVideoHeight != currentVideoHeight) {
            this.currentVideoWidth = currentVideoWidth
            this.currentVideoHeight = currentVideoHeight
            requestLayout()
        }
    }

    fun setDisplayType(type: MXVideoDisplay) {
        if (displayType != type) {
            displayType = type
            requestLayout()
        }
    }

    override fun setRotation(rotation: Float) {
        if (rotation != getRotation()) {
            super.setRotation(rotation)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val videoWidth = currentVideoWidth
        val videoHeight = currentVideoHeight
        var videoRatio = 16 / 9f
        if (videoWidth > 0 && videoHeight > 0) {
            videoRatio = videoWidth.toFloat() / videoHeight
        } else {
            // 默认16：9
            setMeasuredDimension(widthSize, (widthSize / videoRatio).toInt())
            return
        }
        var measureWidth = widthSize
        var measureHeight = videoHeight

        when (displayType) {
            MXVideoDisplay.FILL_PARENT -> {
                if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                    measureWidth = widthSize
                    measureHeight = heightSize
                } else if (widthMode == MeasureSpec.EXACTLY) {
                    measureWidth = widthSize
                    measureHeight = (widthSize / videoRatio).toInt()
                } else if (heightMode == MeasureSpec.EXACTLY) {
                    measureWidth = (heightSize * videoRatio).toInt()
                    measureHeight = heightSize
                }
            }
            MXVideoDisplay.CENTER_CROP -> {
                if (videoWidth / widthSize > videoHeight / heightSize) {
                    measureWidth = widthSize
                    measureHeight = (widthSize / videoRatio).toInt()
                } else {
                    measureWidth = (heightSize * videoRatio).toInt()
                    measureHeight = heightSize
                }
            }
        }
        println("displayType = ${displayType.name} widthMode=$widthMode  widthSize=$widthSize  heightMode=$heightMode  heightSize=$heightSize  measureWidth = $measureWidth   measureHeight = $measureHeight")
        setMeasuredDimension(measureWidth, measureHeight)
    }

    companion object {
        protected const val TAG = "JZResizeTextureView"
    }
}