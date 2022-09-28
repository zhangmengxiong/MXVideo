package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.mx.video.beans.MXState
import com.mx.video.listener.MXVideoListener
import com.mx.video.views.MXViewSet

open class MXVideoStd @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MXVideo(context, attrs, defStyleAttr) {
    override fun getLayoutId(): Int {
        return R.layout.mx_layout_video_std
    }

    private var onStateListener: ((MXState) -> Unit)? = null
    private var onPrepareStartListener: (() -> Unit)? = null
    private var onPreparedListener: (() -> Unit)? = null
    private var onCompleteListener: (() -> Unit)? = null
    private var onErrorListener: (() -> Unit)? = null
    private var onEmptyPlayListener: (() -> Unit)? = null

    private var onPlayTicketListener: ((position: Int, duration: Int) -> Unit)? = null
    private var onVideoSizeListener: ((width: Int, height: Int) -> Unit)? = null
    private var onBufferListener: ((inBuffer: Boolean) -> Unit)? = null

    init {
        addOnVideoListener(object : MXVideoListener() {
            override fun onStateChange(state: MXState, viewSet: MXViewSet) {
                onStateListener?.invoke(state)
                when (state) {
                    MXState.PREPARING -> {
                        onPrepareStartListener?.invoke()
                    }
                    MXState.PREPARED -> {
                        onPreparedListener?.invoke()
                    }
                    MXState.COMPLETE -> {
                        onCompleteListener?.invoke()
                    }
                    MXState.ERROR -> {
                        onErrorListener?.invoke()
                    }
                    else -> {}
                }
            }

            override fun onPlayTicket(position: Int, duration: Int) {
                onPlayTicketListener?.invoke(position, duration)
            }

            override fun onBuffering(inBuffer: Boolean) {
                onBufferListener?.invoke(inBuffer)
            }

            override fun onEmptyPlay() {
                if (onEmptyPlayListener != null) {
                    onEmptyPlayListener?.invoke()
                } else {
                    Toast.makeText(
                        this@MXVideoStd.context,
                        R.string.mx_play_source_not_set,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onVideoSizeChange(width: Int, height: Int) {
                onVideoSizeListener?.invoke(width, height)
            }
        })
    }

    /**
     * 回调：状态监听
     */
    fun setOnStateListener(listener: ((state: MXState) -> Unit)?) {
        onStateListener = listener
    }

    /**
     * 回调：视频准备启动播放
     */
    fun setOnPrepareStartListener(listener: (() -> Unit)?) {
        onPrepareStartListener = listener
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
    fun setOnVideoSizeListener(listener: ((width: Int, height: Int) -> Unit)?) {
        onVideoSizeListener = listener
    }

    /**
     * 回调：视频播放时间变更
     */
    fun setOnPlayTicketListener(listener: ((position: Int, duration: Int) -> Unit)?) {
        onPlayTicketListener = listener
    }

    /**
     * 回调：没有源时点击播放按钮
     */
    fun setOnEmptyPlayListener(listener: (() -> Unit)?) {
        onEmptyPlayListener = listener
    }

    override fun release() {
        onStateListener = null
        onPrepareStartListener = null
        onPreparedListener = null
        onCompleteListener = null
        onErrorListener = null
        onEmptyPlayListener = null

        onPlayTicketListener = null
        onVideoSizeListener = null
        onBufferListener = null

        super.release()
    }
}