package com.mx.video.beans

import com.mx.video.utils.MXValueObservable
import com.mx.video.utils.MXVideoListener
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * 播放属性配置
 */
class MXConfig : Serializable {
    companion object {
        private val videoViewIndex = AtomicInteger(1)
    }

    /**
     * 当前View的ID，全局ID
     */
    val viewIndexId = videoViewIndex.incrementAndGet()

    /**
     * 播放状态
     */
    val state = MXValueObservable(MXState.IDLE, true)

    /**
     * 全屏状态
     */
    val screen = MXValueObservable(MXScreen.NORMAL)

    /**
     * 播放进度
     * first = 当前播放进度 秒
     * second = 视频总长度 秒
     */
    val position = MXValueObservable(Pair(-1, -1), true)

    /**
     * 预加载状态
     */
    val isPreloading = MXValueObservable(false)

    /**
     * 视频加载中状态
     */
    val loading = MXValueObservable(false)

    /**
     * 旋转角度
     */
    val orientation = MXValueObservable(MXOrientation.DEGREE_0)

    /**
     * 视频宽高
     */
    val videoSize = MXValueObservable(Pair(16, 9))

    val playerViewSize = MXValueObservable(Pair(0, 0))

    /**
     * 视频缩放
     */
    val scale = MXValueObservable(MXScale.CENTER_CROP)

    /**
     * 跳转位置，当>=0时播放前会跳转到对应位置
     * 单位：秒
     */
    val seekWhenPlay = MXValueObservable(-1)

    /**
     * 播放源
     */
    val source = MXValueObservable<MXPlaySource?>(null)

    /**
     * 是否可以通过滑动或者进度条调整进度
     */
    val canSeekByUser = MXValueObservable(true)

    /**
     * 是否显示全屏按钮
     */
    val showFullScreenBtn = MXValueObservable(true)

    /**
     * 是否显示右上角的时间
     */
    val canShowSystemTime = MXValueObservable(true)

    /**
     * 是否显示底部进度条
     */
    val canShowBottomSeekBar = MXValueObservable(true)

    /**
     * 是否显示右上角的电量信息
     */
    val canShowBatteryImg = MXValueObservable(true)

    /**
     * 当非WiFi网络是是否弹出提示
     */
    val showTipIfNotWifi = MXValueObservable(true)

    /**
     * 播放完成时如果是全屏，则退出全屏
     */
    val gotoNormalScreenWhenComplete = MXValueObservable(true)

    /**
     * 播放错误时如果是全屏，则退出全屏
     */
    val gotoNormalScreenWhenError = MXValueObservable(true)

    /**
     * 播放时用户可以暂停  ~~为啥需要这个？
     */
    val canPauseByUser = MXValueObservable(true)

    /**
     * 播放时随着感应器旋转而全屏/小屏
     */
    val autoRotateBySensor = MXValueObservable(true)

    /**
     * 直播流，播放失败时自动重新播放
     */
    val replayLiveSourceWhenError = MXValueObservable(true)

    /**
     * 是否可以快进快退
     */
    fun sourceCanSeek(): Boolean {
        return canSeekByUser.get() && (source.get()?.isLiveSource != true)
    }

    /**
     * 全屏时是否变更屏幕方向
     */
    fun willChangeOrientationWhenFullScreen(): Boolean {
        if (source.get()?.changeOrientationWhenFullScreen == true) {
            return true
        }
        val size = videoSize.get()
        return size.first > size.second
    }

    /**
     * 监听器列表
     */
    val videoListeners = ArrayList<MXVideoListener>()

    fun cloneBy(target: MXConfig) {
        orientation.set(target.orientation.get())
        videoSize.set(
            Pair(target.videoSize.get().first, target.videoSize.get().second)
        )
        scale.set(target.scale.get())
        seekWhenPlay.set(target.seekWhenPlay.get())
        source.set(target.source.get()?.clone())
        canSeekByUser.set(target.canSeekByUser.get())
        showFullScreenBtn.set(target.showFullScreenBtn.get())
        canShowSystemTime.set(target.canShowSystemTime.get())
        canShowBottomSeekBar.set(target.canShowBottomSeekBar.get())
        canShowBatteryImg.set(target.canShowBatteryImg.get())
        showTipIfNotWifi.set(target.showTipIfNotWifi.get())
        gotoNormalScreenWhenComplete.set(target.gotoNormalScreenWhenComplete.get())
        gotoNormalScreenWhenError.set(target.gotoNormalScreenWhenError.get())
        canPauseByUser.set(target.canPauseByUser.get())
        autoRotateBySensor.set(target.autoRotateBySensor.get())
        replayLiveSourceWhenError.set(target.replayLiveSourceWhenError.get())
        playerViewSize.set(
            Pair(
                target.playerViewSize.get().first,
                target.playerViewSize.get().second
            )
        )
    }

    fun reset() {
        state.set(MXState.IDLE)
        orientation.set(MXOrientation.DEGREE_0)
        videoSize.set(Pair(16, 9))
        scale.set(MXScale.CENTER_CROP)
        seekWhenPlay.set(-1)
        source.set(null)
        canSeekByUser.set(true)
        showFullScreenBtn.set(true)
        canShowSystemTime.set(true)
        canShowBottomSeekBar.set(true)
        canShowBatteryImg.set(true)
        showTipIfNotWifi.set(true)
        gotoNormalScreenWhenComplete.set(true)
        gotoNormalScreenWhenError.set(true)
        canPauseByUser.set(true)
        autoRotateBySensor.set(false)
        replayLiveSourceWhenError.set(false)
        playerViewSize.set(0 to 0)
    }

    fun release() {
        videoListeners.clear()
    }
}