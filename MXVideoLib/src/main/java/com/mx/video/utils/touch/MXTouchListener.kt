package com.mx.video.utils.touch

open class MXTouchListener {
    open fun touchStart() {}
    open fun touchMove(percent: Float) {}
    open fun touchEnd(percent: Float) {}
    open fun release() {}
}