package com.mx.video.beans

/**
 * 旋转模式
 */
enum class MXSensorMode {
    /**
     * 跟随重力方向
     */
    SENSOR_AUTO,

    /**
     * 跟随视频宽高自动旋转 0 或 180 度
     */
    SENSOR_FIT_VIDEO,

    /**
     * 根据视频宽高比固定横屏/竖屏
     * 横屏 = 视频宽>=高
     * 竖屏 = 视频宽<高
     */
    SENSOR_NO;
}