package com.mx.video.beans

import android.media.AudioAttributes
import com.mx.video.listener.MXVideoListener
import com.mx.video.utils.MXValueObservable
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
     * 声音大小百分比
     * 静音 = 0f ，   默认 = 1f
     */
    internal val volumePercent = MXValueObservable(1f)

    /**
     * 视频宽高
     */
    internal val videoSize = MXValueObservable(MXSize(0, 0))

    internal val playerViewSize = MXValueObservable(MXSize(0, 0))

    /**
     * 视频缩放
     */
    internal val scale = MXValueObservable(MXScale.CENTER_CROP)

    /**
     * 播放器宽高比
     */
    internal val dimensionRatio = MXValueObservable(0.0)

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
     * 水平镜像模式
     */
    val mirrorMode = MXValueObservable(false)

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
     * 是否显示网速信息
     */
    val canShowNetSpeed = MXValueObservable(true)

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
    val replayLiveSourceWhenError = MXValueObservable(false)

    /**
     * 是否可以快进快退
     */
    internal fun sourceCanSeek(): Boolean {
        return canSeekByUser.get() && (source.get()?.isLiveSource != true)
    }

    /**
     * 全屏时是否变更屏幕方向
     */
    internal fun willChangeOrientationWhenFullScreen(): Boolean {
        if (source.get()?.changeOrientationWhenFullScreen == true) {
            return true
        }
        val size = videoSize.get()
        return size.width > size.height
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
        autoRotateBySensorWhenFullScreen.set(target.autoRotateBySensorWhenFullScreen.get())
        replayLiveSourceWhenError.set(target.replayLiveSourceWhenError.get())
        playerViewSize.set(target.playerViewSize.get().clone())
    }

    internal fun release() {
        videoListeners.clear()
        for (field in this::class.java.declaredFields) {
            val any = field.get(this)
            if (any is MXValueObservable<*>) {
//                MXUtils.log("${field.name} -> deleteObservers")
                any.deleteObservers()
            }
        }
    }
}