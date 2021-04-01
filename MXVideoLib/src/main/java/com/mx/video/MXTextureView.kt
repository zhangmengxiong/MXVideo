package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import com.mx.video.utils.MXUtils

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
                    if (videoWidth / widthSize > videoHeight / heightSize) {
                        width = widthSize
                        height = (widthSize / videoRatio).toInt()
                    } else {
                        width = (heightSize * videoRatio).toInt()
                        height = heightSize
                        if (width > widthSize) {
                            width = widthSize
                            height = (height * (widthSize.toDouble() / width)).toInt()
                        }
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
        MXUtils.log("${displayType.name} specMode=$widthMode x $heightMode  specSize=$widthSize x $heightSize  videoSize=$videoWidth x $videoWidth  size=$width x $height")
        setMeasuredDimension(width, height)
    }
}