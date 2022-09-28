package com.mx.video.views

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.TextureView
import com.mx.video.beans.MXConfig
import com.mx.video.beans.MXOrientation
import com.mx.video.beans.MXScale
import com.mx.video.beans.MXSize
import com.mx.video.utils.MXValueObservable

class MXTextureView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr) {
    private val size = MXValueObservable(MXSize(0, 0))

    private val sizeObserver = { _: MXSize ->
        resetTransform()
    }
    private val scaleObserver = { _: MXScale ->
        resetTransform()
    }
    private val orientationObserver = { _: MXOrientation ->
        resetTransform()
    }
    private val mirrorObserver = { _: Boolean ->
        resetTransform()
    }

    init {
        isFocusable = false
        isFocusableInTouchMode = false
        size.addObserver(sizeObserver)
    }

    private var config: MXConfig? = null
    fun setConfig(config: MXConfig) {
        if (this.config != null) {
            this.config?.videoSize?.deleteObserver(sizeObserver)
            this.config?.scale?.deleteObserver(scaleObserver)
            this.config?.orientation?.deleteObserver(orientationObserver)
            this.config?.mirrorMode?.deleteObserver(mirrorObserver)
        }

        config.videoSize.addObserver(sizeObserver)
        config.scale.addObserver(scaleObserver)
        config.orientation.addObserver(orientationObserver)
        config.mirrorMode.addObserver(mirrorObserver)

        this.config = config
    }

    fun release() {
        this.config?.let { config ->
            config.videoSize.deleteObserver(sizeObserver)
            config.scale.deleteObserver(scaleObserver)
            config.orientation.deleteObserver(orientationObserver)
            config.mirrorMode.deleteObserver(mirrorObserver)
        }
        this.config = null
        surfaceTextureListener = null
        size.release()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        size.set(MXSize(w, h))
    }

    private val mxMatrix = Matrix()
    private fun resetTransform() {
        val config = config
        val viewWidth = size.get().width.toFloat()
        val viewHeight = size.get().height.toFloat()
        val sx = viewWidth / 2f
        val sy = viewHeight / 2f
        if (config == null || viewWidth <= 0 || viewHeight <= 0) {
            // 数据校验
            setTransform(null)
            return
        }
        val orientation = config.orientation.get()
        val scale = config.scale.get()
        val mirror = config.mirrorMode.get()

        val videoSize = config.videoSize.get()
        if (videoSize.width <= 0 || videoSize.height <= 0) {
            // 数据校验
            setTransform(null)
            return
        }

        // 视频宽高比
        val videoRatio = videoSize.width.toFloat() / videoSize.height.toFloat()

        mxMatrix.reset()
        mxMatrix.setRotate(orientation.degree.toFloat(), sx, sy)
        if (orientation.isVertical()) {
            val target = getScaleCenterCrop(scale, videoRatio, viewWidth, viewHeight)

            mxMatrix.postScale(
                target.first / viewWidth,
                target.second / viewHeight,
                sx, sy
            )
        } else if (orientation.isHorizontal()) {
            val scaleX = viewHeight / viewWidth
            val scaleY = viewWidth / viewHeight
            if (scale == MXScale.CENTER_CROP) {
                val target = getScaleCenterCrop(
                    scale,
                    1f / videoRatio,
                    viewWidth,
                    viewHeight
                )
                mxMatrix.postScale(
                    scaleY * target.first / viewWidth,
                    scaleX * target.second / viewHeight,
                    sx, sy
                )
            } else {
                mxMatrix.postScale(scaleY, scaleX, sx, sy)
            }
        }
        if (mirror) {
            mxMatrix.postScale(-1f, 1f, sx, sy)
        }

        setTransform(mxMatrix)
    }

    /**
     * 计算缩放后的大小
     */
    private fun getScaleCenterCrop(
        scale: MXScale, videoRatio: Float,
        w: Float, h: Float
    ): Pair<Float, Float> {
        return when (scale) {
            MXScale.FILL_PARENT -> {
                Pair(w, h)
            }
            MXScale.CENTER_CROP -> {
                if (videoRatio > w / h) {
                    Pair(w, w / videoRatio)
                } else {
                    Pair(videoRatio * h, h)
                }
            }
        }
    }
}