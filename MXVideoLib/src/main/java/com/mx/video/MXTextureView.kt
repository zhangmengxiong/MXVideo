package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import com.mx.video.utils.MXUtils

class MXTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var displayType = MXVideoDisplay.CENTER_CROP

    fun setVideoSize(mVideoWidth: Int, mVideoHeight: Int) {
        if (this.mVideoWidth != mVideoWidth || this.mVideoHeight != mVideoHeight) {
            this.mVideoWidth = mVideoWidth
            this.mVideoHeight = mVideoHeight
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
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec

        val viewRotation = rotation.toInt()
        val videoWidth: Int = mVideoWidth
        var videoHeight: Int = mVideoHeight


        var parentHeight = (parent as View).measuredHeight
        var parentWidth = (parent as View).measuredWidth
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (displayType === MXVideoDisplay.FILL_PARENT) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**强制充满 */
                videoHeight = videoWidth * parentHeight / parentWidth
            }
        }

        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
        if (viewRotation == 90 || viewRotation == 270) {
            widthMeasureSpec = heightMeasureSpec
            heightMeasureSpec = widthMeasureSpec
        }

        var width = getDefaultSize(videoWidth, widthMeasureSpec)
        var height = getDefaultSize(videoHeight, heightMeasureSpec)
        if (videoWidth > 0 && videoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            MXUtils.log("widthMeasureSpec  [" + MeasureSpec.toString(widthMeasureSpec) + "]")
            MXUtils.log("heightMeasureSpec [" + MeasureSpec.toString(heightMeasureSpec) + "]")
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * videoHeight / videoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * videoWidth / videoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth
                height = videoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                }
            }
        } else {
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            width = widthSpecSize
            height = (widthSpecSize * (9 / 16f)).toInt()
        }
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            if (displayType == MXVideoDisplay.CENTER_CROP) {
                if (viewRotation == 90 || viewRotation == 270) {
                    val tempSize = parentWidth
                    parentWidth = parentHeight
                    parentHeight = tempSize
                }
                /**充满剪切 */
                if (videoHeight.toDouble() / videoWidth > parentHeight.toDouble() / parentWidth) {
                    height = (parentWidth.toDouble() / width.toDouble() * height.toDouble()).toInt()
                    width = parentWidth
                } else if (videoHeight.toDouble() / videoWidth < parentHeight.toDouble() / parentWidth) {
                    width = (parentHeight.toDouble() / height.toDouble() * width.toDouble()).toInt()
                    height = parentHeight
                }
            }
        }
        setMeasuredDimension(width, height)
    }
}