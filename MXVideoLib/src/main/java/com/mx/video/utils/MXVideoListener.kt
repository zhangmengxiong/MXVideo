package com.mx.video.utils

import com.mx.video.MXState

open class MXVideoListener {
    /**
     * 状态变化
     */
    open fun onStateChange(state: MXState) = Unit

    /**
     * 播放时间回调
     * @param position 当前播放位置 秒
     * @param duration 总时长 秒
     */
    open fun onPlayTicket(position: Int, duration: Int) = Unit
}