package com.mx.video

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

abstract class MXVideo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        val displayType = MXVideoDisplay.ORIGINAL
    }

    init {
        View.inflate(context, getLayoutId(), this)
    }

    abstract fun getLayoutId(): Int

    fun onPrepared() {

    }

    fun onCompletion() {

    }

    fun setBufferProgress(percent: Int) {

    }

    fun onSeekComplete() {

    }

    fun onError() {

    }

    fun onBuffering(start: Boolean) {

    }

    fun onRenderingStart() {

    }

    fun onVideoSizeChanged(width: Int, height: Int) {

    }
}