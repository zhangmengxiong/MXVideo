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
    internal val viewIndexId = videoViewIndex.incrementAndGet()

    /**
     * 监听器列表
     */
    internal val videoListeners = ArrayList<MXVideoListener>()

    /**
     * 播放状态
     */
    internal val state = MXValueObservable(MXState.IDLE)

    /**
     * 全屏状态
     */
    internal val screen = MXValueObservable(MXScreen.NORMAL)

    /**
     * 预加载状态
     */
    internal val isPreloading = MXValueObservable(false)

    /**
     * 视频加载中状态
     */
    internal val loading = MXValueObservable(false)

    /**
     * 旋转角度
     */
    internal val orientation = MXValueObservable(MXOrientation.DEGREE_0)

    /**
     * 视频宽高
     */
    internal val videoSize = MXValueObservable(MXSize(16, 9))

    internal val playerViewSize = MXValueObservable(MXSize(0, 0), true)

    /**
     * 视频缩放
     */
    internal val scale = MXValueObservable(MXScale.CENTER_CROP)

    /**
     * 跳转位置，当>=0时播放前会跳转到对应位置
     * 单位：秒
     */
    internal val seekWhenPlay = MXValueObservable(-1)

    /**
     * 播放源
     */
    internal val source = MXValueObservable<MXPlaySource?>(null)

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
     * 播放时随着感应器旋转自动切换成全屏
     */
    val autoFullScreenBySensor = MXValueObservable(false)

    /**
     * 播放时随着感应器旋转而全屏/小屏
     */
    val autoRotateBySensorWhenFullScreen = MXValueObservable(true)

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
        return size.width > size.height
    }

    fun cloneBy(target: MXConfig) {
        orientation.reset(target.orientation.get())
        videoSize.reset(target.videoSize.get().clone())
        scale.reset(target.scale.get())
        seekWhenPlay.reset(target.seekWhenPlay.get())
        source.reset(target.source.get()?.clone())
        canSeekByUser.reset(target.canSeekByUser.get())
        showFullScreenBtn.reset(target.showFullScreenBtn.get())
        canShowSystemTime.reset(target.canShowSystemTime.get())
        canShowBottomSeekBar.reset(target.canShowBottomSeekBar.get())
        canShowBatteryImg.reset(target.canShowBatteryImg.get())
        showTipIfNotWifi.reset(target.showTipIfNotWifi.get())
        gotoNormalScreenWhenComplete.reset(target.gotoNormalScreenWhenComplete.get())
        gotoNormalScreenWhenError.reset(target.gotoNormalScreenWhenError.get())
        canPauseByUser.reset(target.canPauseByUser.get())
        autoFullScreenBySensor.reset(target.autoFullScreenBySensor.get())
        autoRotateBySensorWhenFullScreen.reset(target.autoRotateBySensorWhenFullScreen.get())
        replayLiveSourceWhenError.reset(target.replayLiveSourceWhenError.get())
        playerViewSize.reset(target.playerViewSize.get().clone())
    }

    fun reset() {
        state.reset(MXState.IDLE)
        orientation.reset(MXOrientation.DEGREE_0)
        videoSize.reset(MXSize(16, 9))
        scale.reset(MXScale.CENTER_CROP)
        seekWhenPlay.reset(-1)
        source.reset(null)
        canSeekByUser.reset(true)
        showFullScreenBtn.reset(true)
        canShowSystemTime.reset(true)
        canShowBottomSeekBar.reset(true)
        canShowBatteryImg.reset(true)
        showTipIfNotWifi.reset(true)
        gotoNormalScreenWhenComplete.reset(true)
        gotoNormalScreenWhenError.reset(true)
        canPauseByUser.reset(true)
        autoFullScreenBySensor.reset(false)
        autoRotateBySensorWhenFullScreen.reset(true)
        replayLiveSourceWhenError.reset(false)
        playerViewSize.reset(MXSize(0, 0))
    }

    fun release() {
        videoListeners.clear()
    }
}