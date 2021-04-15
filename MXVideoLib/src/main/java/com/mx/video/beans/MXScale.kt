package com.mx.video.beans

import java.io.Serializable


/**
 * 播放缩放类型枚举
 */
enum class MXScale : Serializable {
    FILL_PARENT, // 填充宽高
    CENTER_CROP // 根据视频宽高自适应
}