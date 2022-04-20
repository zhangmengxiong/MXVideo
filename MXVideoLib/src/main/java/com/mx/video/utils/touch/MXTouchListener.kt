package com.mx.video.utils.touch

internal interface MXTouchListener {
    fun touchStart() {}
    fun touchMove(percent: Float) {}
    fun touchEnd(percent: Float) {}
    fun release() {}
}