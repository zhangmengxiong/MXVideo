package com.mx.video.listener

internal interface MXBaseTouchListener {
    fun touchStart() {}
    fun touchMove(percent: Float) {}
    fun touchEnd(percent: Float) {}
    fun release() {}
}