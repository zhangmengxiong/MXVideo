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
     * ?????????????????????
     */
    fun setOnStateListener(listener: ((state: MXState) -> Unit)?) {
        onStateListener = listener
    }

    /**
     * ?????????????????????????????????
     */
    fun setOnPrepareStartListener(listener: (() -> Unit)?) {
        onPrepareStartListener = listener
    }

    /**
     * ???????????????????????????
     */
    fun setOnPreparedListener(listener: (() -> Unit)?) {
        onPreparedListener = listener
    }

    /**
     * ???????????????????????????
     */
    fun setOnCompleteListener(listener: (() -> Unit)?) {
        onCompleteListener = listener
    }

    /**
     * ???????????????????????????
     */
    fun setOnErrorListener(listener: (() -> Unit)?) {
        onErrorListener = listener
    }

    /**
     * ???????????????????????????
     */
    fun setOnBufferListener(listener: ((inBuffer: Boolean) -> Unit)?) {
        onBufferListener = listener
    }

    /**
     * ?????????????????????????????????
     */
    fun setOnVideoSizeListener(listener: ((width: Int, height: Int) -> Unit)?) {
        onVideoSizeListener = listener
    }

    /**
     * ?????????????????????????????????
     */
    fun setOnPlayTicketListener(listener: ((position: Int, duration: Int) -> Unit)?) {
        onPlayTicketListener = listener
    }

    /**
     * ???????????????????????????????????????
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