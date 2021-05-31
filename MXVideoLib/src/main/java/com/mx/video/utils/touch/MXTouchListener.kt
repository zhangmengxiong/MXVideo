package com.mx.video.utils.touch

open class MXTouchListener {
    open fun onStart() {}
    open fun onTouchMove(percent: Float) {}
    open fun onEnd(percent: Float) {}

    open fun release() {}
}