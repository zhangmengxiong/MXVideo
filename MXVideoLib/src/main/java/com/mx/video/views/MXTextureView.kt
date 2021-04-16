package com.mx.video.views

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.mx.video.beans.MXDegree
import com.mx.video.beans.MXScale

class MXTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    private var mVideoWidth = 1280
    private var mVideoHeight = 720
    private var displayType = MXScale.CENTER_CROP

    fun setVideoSize(mVideoWidth: Int, mVideoHeight: Int) {
        if (this.mVideoWidth != mVideoWidth || this.mVideoHeight != mVideoHeight) {
            this.mVideoWidth = mVideoWidth
            this.mVideoHeight = mVideoHeight
            requestLayout()
        }
    }

    fun setDisplayType(type: MXScale) {
        if (displayType != type) {
            displayType = type
            requestLayout()
        }
    }

    fun setDegree(degree: MXDegree) {
        val degree = degree.degree.toFloat()
        if (degree != rotation) {
            super.setRotation(degree)
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val rotation = rotation.toInt() % 360
        if (rotation == 90 || rotation == 270) {
            val tmp = widthMeasureSpec
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = tmp
        }

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val videoWidth = mVideoWidth
        val videoHeight = mVideoHeight
        var videoRatio = 16.0 / 9.0

        if (videoWidth > 0 && videoHeight > 0 && widthSize > 0 && heightSize > 0) {
            videoRatio = videoWidth.toDouble() / videoHeight
        } else {
            // 默认16：9
            setMeasuredDimension(widthSize, (widthSize / videoRatio).toInt())
            return
        }
        var width = widthSize
        var height = (widthSize / videoRatio).toInt()

        when (displayType) {
            MXScale.FILL_PARENT -> {
                if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                    width = widthSize
                    height = heightSize
                } else if (widthMode == MeasureSpec.EXACTLY) {
                    width = widthSize
                    height = (widthSize / videoRatio).toInt()
                } else if (heightMode == MeasureSpec.EXACTLY) {
                    width = (heightSize * videoRatio).toInt()
                    height = heightSize
                }
            }
            MXScale.CENTER_CROP -> {
                if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                    width = widthSize
                    height = (widthSize / videoRatio).toInt()
                    if (height > heightSize) {
                        val scale = heightSize / height.toDouble()
                        width = (width * scale).toInt()
                        height = heightSize
                    }
                } else if (widthMode == MeasureSpec.EXACTLY) {
                    width = widthSize
                    height = (widthSize / videoRatio).toInt()
                } else if (heightMode == MeasureSpec.EXACTLY) {
                    width = (heightSize * videoRatio).toInt()
                    height = heightSize
                }
            }
        }
//        MXUtils.log("${displayType.name} specMode=$widthMode x $heightMode  specSize=$widthSize x $heightSize  videoSize=$videoWidth x $videoHeight  size=$width x $height")
        setMeasuredDimension(width, height)
    }
}