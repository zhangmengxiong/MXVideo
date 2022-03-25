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
     * 视频SurfaceView的旋转角度
     */
    internal val orientation = MXValueObservable(MXOrientation.DEGREE_0)

    /**
     * 视频宽高
     */
    internal val videoSize = MXValueObservable(MXSize(1280, 720))

    internal val playerViewSize = MXValueObservable(MXSize(0, 0))

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
     * 是否能进入全屏
     */
    val canFullScreen = MXValueObservable(true)
    val showFullScreenButton = MXValueObservable(true)

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
        orientation.set(target.orientation.get())
        videoSize.set(target.videoSize.get().clone())
        scale.set(target.scale.get())
        seekWhenPlay.set(target.seekWhenPlay.get())
        source.set(target.source.get()?.clone())
        canSeekByUser.set(target.canSeekByUser.get())
        canFullScreen.set(target.canFullScreen.get())
        showFullScreenButton.set(target.showFullScreenButton.get())
        canShowSystemTime.set(target.canShowSystemTime.get())
        canShowBottomSeekBar.set(target.canShowBottomSeekBar.get())
        canShowBatteryImg.set(target.canShowBatteryImg.get())
        showTipIfNotWifi.set(target.showTipIfNotWifi.get())
        gotoNormalScreenWhenComplete.set(target.gotoNormalScreenWhenComplete.get())
        gotoNormalScreenWhenError.set(target.gotoNormalScreenWhenError.get())
        canPauseByUser.set(target.canPauseByUser.get())
        autoFullScreenBySensor.set(target.autoFullScreenBySensor.get())
        autoRotateBySensorWhenFullScreen.set(target.autoRotateBySensorWhenFullScreen.get())
        replayLiveSourceWhenError.set(target.replayLiveSourceWhenError.get())
        playerViewSize.set(target.playerViewSize.get().clone())
    }

    fun reset() {
        state.set(MXState.IDLE)
        screen.set(MXScreen.NORMAL)
        isPreloading.set(false)
        orientation.set(MXOrientation.DEGREE_0)
        videoSize.set(MXSize(1280, 720))
        scale.set(MXScale.CENTER_CROP)
        seekWhenPlay.set(-1)
        source.set(null)
        canSeekByUser.set(true)
        canFullScreen.set(true)
        showFullScreenButton.set(true)
        canShowSystemTime.set(true)
        canShowBottomSeekBar.set(true)
        canShowBatteryImg.set(true)
        showTipIfNotWifi.set(true)
        gotoNormalScreenWhenComplete.set(true)
        gotoNormalScreenWhenError.set(true)
        canPauseByUser.set(true)
        autoFullScreenBySensor.set(false)
        autoRotateBySensorWhenFullScreen.set(true)
        replayLiveSourceWhenError.set(false)
        playerViewSize.set(MXSize(0, 0))
    }

    fun release() {
        videoListeners.clear()
    }
}