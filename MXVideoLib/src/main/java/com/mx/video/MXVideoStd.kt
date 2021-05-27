package com.mx.video

import android.content.Context
import android.util.AttributeSet
import com.mx.video.beans.MXState
import com.mx.video.utils.MXVideoListener
import com.mx.video.views.MXViewProvider

class MXVideoStd @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MXVideo(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.mx_layout_video_std
    }

    private var onCompleteListener: (() -> Unit)? = null
    private var onErrorListener: (() -> Unit)? = null
    private var onPreparedListener: (() -> Unit)? = null

    private var onTimeListener: ((position: Int, duration: Int) -> Unit)? = null
    private var onBufferListener: ((inBuffer: Boolean) -> Unit)? = null
    private val videoListener = object : MXVideoListener() {
        override fun onStateChange(state: MXState, provider: MXViewProvider) {
            when (state) {
                MXState.PREPARED -> {
                    onPreparedListener?.invoke()
                }
                MXState.COMPLETE -> {
                    onCompleteListener?.invoke()
                }
                MXState.ERROR -> {
                    onErrorListener?.invoke()
                }
            }
        }

        override fun onPlayTicket(position: Int, duration: Int) {
            onTimeListener?.invoke(position, duration)
        }

        override fun onBuffering(inBuffer: Boolean) {
            onBufferListener?.invoke(inBuffer)
        }
    }

    init {
        addOnVideoListener(videoListener)
    }

    /**
     * 回调：视频已准备好
     */
    fun setOnPreparedListener(listener: (() -> Unit)?) {
        onPreparedListener = listener
    }

    /**
     * 回调：视频播放完成
     */
    fun setOnCompleteListener(listener: (() -> Unit)?) {
        onCompleteListener = listener
    }

    /**
     * 回调：视频播放错误
     */
    fun setOnErrorListener(listener: (() -> Unit)?) {
        onErrorListener = listener
    }

    /**
     * 回调：视频缓冲加载
     */
    fun setOnBufferListener(listener: ((inBuffer: Boolean) -> Unit)?) {
        onBufferListener = listener
    }


    /**
     * 回调：视频播放时间变更
     */
    fun setOnTimeListener(listener: ((position: Int, duration: Int) -> Unit)?) {
        onTimeListener = listener
    }
}