package com.mx.video.beans

import java.io.Serializable


/**
 * 播放缩放类型枚举
 */
enum class MXScale : Serializable {
    /**
     * 填充宽高
     */
    FILL_PARENT,

    /**
     * 根据视频宽高自适应
     */
    CENTER_CROP
}