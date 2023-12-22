package com.mx.video.listener

import com.mx.video.beans.MXPlaySource
import com.mx.video.beans.MXScreen
import com.mx.video.beans.MXState
import com.mx.video.views.MXViewSet

open class MXVideoListener {
    /**
     * 状态变化
     */
    open fun onStateChange(state: MXState, viewSet: MXViewSet) = Unit

    /**
     * 播放时间回调
     * @param position 当前播放位置 秒
     * @param duration 总时长 秒
     */
    open fun onPlayTicket(position: Int, duration: Int) = Unit

    /**
     * 全屏/小屏 状态监听
     */
    open fun onScreenChange(screen: MXScreen, viewSet: MXViewSet) = Unit

    /**
     * 视频宽高监听
     */
    open fun onVideoSizeChange(width: Int, height: Int) = Unit

    /**
     * 加载缓冲监听
     */
    open fun onBuffering(inBuffer: Boolean) = Unit

    /**
     * 错误回调
     */
    open fun onError(source: MXPlaySource, message: String) = Unit

    /**
     * 没有播放源时，点击播放回调
     */
    open fun onEmptyPlay() = Unit
}