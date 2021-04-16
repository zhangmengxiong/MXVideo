package com.mx.video.beans

/**
 * 手机旋转角度
 */
enum class MXDegree {
    DEGREE_0,
    DEGREE_90,
    DEGREE_180,
    DEGREE_270;

    /**
     * 是否横屏
     */
    fun isHorizontal(): Boolean {
        return this == DEGREE_90 || this == DEGREE_270
    }

    /**
     * 是否竖屏
     */
    fun isVertical(): Boolean {
        return this == DEGREE_0 || this == DEGREE_180
    }
}