package com.mx.video.utils

import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.views.MXViewProvider

open class MXVideoListener {
    /**
     * 状态变化
     */
    open fun onStateChange(state: MXState, provider: MXViewProvider) = Unit

    /**
     * 播放时间回调
     * @param position 当前播放位置 秒
     * @param duration 总时长 秒
     */
    open fun onPlayTicket(position: Int, duration: Int) = Unit

    /**
     * 全屏/小屏 状态监听
     */
    open fun onScreenChange(screen: MXScreen, provider: MXViewProvider) = Unit

    /**
     * 视频宽高监听
     */
    open fun onVideoSizeChange(width: Int, height: Int) = Unit

    /**
     * 加载缓冲监听
     */
    open fun onBuffering(inBuffer: Boolean) = Unit
}