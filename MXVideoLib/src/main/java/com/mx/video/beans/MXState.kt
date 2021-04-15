package com.mx.video.beans

import java.io.Serializable


/**
 * 播放状态枚举
 */
enum class MXState : Serializable {
    /**
     * 初始状态
     */
    IDLE,

    /**
     * 已设置数据源，但未开始
     */
    NORMAL,

    /**
     * 视频资源校验准备中
     */
    PREPARING,

    /**
     * 预加载视频完成，等待开始播放
     */
    PREPARED,

    /**
     * 播放中
     */
    PLAYING,

    /**
     * 暂停中
     */
    PAUSE,

    /**
     * 播放完成
     */
    COMPLETE,

    /**
     * 播放错误
     */
    ERROR
}