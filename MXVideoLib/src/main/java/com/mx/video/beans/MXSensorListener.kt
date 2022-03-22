package com.mx.video.beans

/**
 * 设备旋转监听接口
 */
internal interface MXSensorListener {
    fun onChange(orientation: MXOrientation)
}