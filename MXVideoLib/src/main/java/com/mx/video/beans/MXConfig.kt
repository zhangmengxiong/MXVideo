package com.mx.video.beans

import com.mx.video.listener.MXVideoListener
import com.mx.video.utils.MXObservable
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
    internal val state = MXObservable(MXState.IDLE)

    /**
     * 全屏状态
     */
    internal val screen = MXObservable(MXScreen.NORMAL)

    /**
     * 预加载状态
     */
    internal val isPreloading = MXObservable(false)

    /**
     * 视频加载中状态
     */
    internal val loading = MXObservable(false)

    /**
     * 视频SurfaceView的旋转角度
     */
    internal val orientation = MXObservable(MXOrientation.DEGREE_0)

    /**
     * 声音大小百分比
     * 静音 = 0f ，   默认 = 1f
     */
    internal val volumePercent = MXObservable(1f)

    /**
     * 视频宽高
     */
    internal val videoSize = MXObservable(MXSize(0, 0))

    internal val playerViewSize = MXObservable(MXSize(0, 0))

    /**
     * 视频缩放
     */
    internal val scale = MXObservable(MXScale.CENTER_CROP)

    /**
     * 播放器宽高比
     */
    internal val dimensionRatio = MXObservable(0.0)

    /**
     * 跳转位置，当>=0时播放前会跳转到对应位置
     * 单位：秒
     */
    internal val seekWhenPlay = MXObservable(-1)

    /**
     * 播放源
     */
    internal val source = MXObservable<MXPlaySource?>(null)


    /**
     * 是否可以通过滑动或者进度条调整进度
     */
    val canSeekByUser = MXObservable(true)

    /**
     * 水平镜像模式
     */
    val mirrorMode = MXObservable(false)

    /**
     * 是否能进入全屏
     */
    val canFullScreen = MXObservable(true)
    val showFullScreenButton = MXObservable(true)

    /**
     * 是否显示右上角的时间
     */
    val canShowSystemTime = MXObservable(true)

    /**
     * 是否显示网速信息
     */
    val canShowNetSpeed = MXObservable(true)

    /**
     * 是否显示底部进度条
     */
    val canShowBottomSeekBar = MXObservable(true)

    /**
     * 是否显示右上角的电量信息
     */
    val canShowBatteryImg = MXObservable(true)

    /**
     * 当非WiFi网络是是否弹出提示
     */
    val showTipIfNotWifi = MXObservable(true)

    /**
     * 播放完成时如果是全屏，则退出全屏
     */
    val gotoNormalScreenWhenComplete = MXObservable(true)

    /**
     * 播放错误时如果是全屏，则退出全屏
     */
    val gotoNormalScreenWhenError = MXObservable(true)

    /**
     * 播放时用户可以暂停  ~~为啥需要这个？
     */
    val canPauseByUser = MXObservable(true)

    /**
     * 播放时随着感应器旋转自动切换成全屏
     */
    val autoFullScreenBySensor = MXObservable(false)

    /**
     * 全屏播放时屏幕方向处理模式
     */
    val fullScreenSensorMode = MXObservable(MXSensorMode.SENSOR_FIT_VIDEO)

    /**
     * 直播流，播放失败时自动重新播放
     */
    val replayLiveSourceWhenError = MXObservable(false)

    /**
     * 没有设置source时不显示播放按钮
     */
    val hidePlayBtnWhenNoSource = MXObservable(false)

    /**
     * 竖屏模式下是否支持滑动快进快退/音量调节/亮度调节功能
     */
    val enableTouchWhenNormalScreen = MXObservable(false)

    /**
     * 显示/隐藏动画的时长
     */
    val animatorDuration = MXObservable(200L)

    /**
     * 计时器间隔 单位：毫秒
     */
    val ticketDiff = MXObservable(1000L)

    /**
     * 播放倍数
     */
    val playSpeed = MXObservable(1f)

    /**
     * 是否可以快进快退
     */
    internal fun sourceCanSeek(): Boolean {
        return canSeekByUser.get() && (source.get()?.isLiveSource != true)
    }

    internal fun cloneBy(target: MXConfig) {
        orientation.set(target.orientation.get())
        volumePercent.set(target.volumePercent.get())
        videoSize.set(target.videoSize.get().clone())
        scale.set(target.scale.get())
        dimensionRatio.set(target.dimensionRatio.get())
        seekWhenPlay.set(target.seekWhenPlay.get())
        source.set(target.source.get()?.clone())
        canSeekByUser.set(target.canSeekByUser.get())
        mirrorMode.set(target.mirrorMode.get())
        canFullScreen.set(target.canFullScreen.get())
        showFullScreenButton.set(target.showFullScreenButton.get())
        canShowSystemTime.set(target.canShowSystemTime.get())
        canShowNetSpeed.set(target.canShowNetSpeed.get())
        canShowBottomSeekBar.set(target.canShowBottomSeekBar.get())
        canShowBatteryImg.set(target.canShowBatteryImg.get())
        showTipIfNotWifi.set(target.showTipIfNotWifi.get())
        gotoNormalScreenWhenComplete.set(target.gotoNormalScreenWhenComplete.get())
        gotoNormalScreenWhenError.set(target.gotoNormalScreenWhenError.get())
        canPauseByUser.set(target.canPauseByUser.get())
        autoFullScreenBySensor.set(target.autoFullScreenBySensor.get())
        fullScreenSensorMode.set(target.fullScreenSensorMode.get())
        replayLiveSourceWhenError.set(target.replayLiveSourceWhenError.get())
        playerViewSize.set(target.playerViewSize.get().clone())
        hidePlayBtnWhenNoSource.set(target.hidePlayBtnWhenNoSource.get())
        animatorDuration.set(target.animatorDuration.get())
        enableTouchWhenNormalScreen.set(target.enableTouchWhenNormalScreen.get())
        ticketDiff.set(target.ticketDiff.get())
        playSpeed.set(target.playSpeed.get())
    }

    internal fun release() {
        videoListeners.clear()
        for (field in this::class.java.declaredFields) {
            val any = field.get(this)
            if (any is MXObservable<*>) {
//                MXUtils.log("${field.name} -> release")
                any.release()
            }
        }
    }
}