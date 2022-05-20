package com.mx.video.listener

import com.mx.video.beans.MXOrientation

/**
 * 设备旋转监听接口
 */
internal interface MXSensorListener {
    fun onChange(orientation: MXOrientation)
}