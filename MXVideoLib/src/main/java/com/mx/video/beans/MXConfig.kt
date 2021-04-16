package com.mx.video.beans

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

    init {
        reset()
    }

    /**
     * 当前View的ID，全局ID
     */
    val viewIndexId = videoViewIndex.incrementAndGet()

    /**
     * 预加载状态
     */
    var isPreloading = false

    /**
     * 旋转角度
     */
    var degree: MXDegree = MXDegree.DEGREE_0

    /**
     * 视频宽度
     */
    var videoWidth: Int = 16

    /**
     * 视频高度
     */
    var videoHeight: Int = 9

    /**
     * 视频缩放
     */
    var scale: MXScale = MXScale.CENTER_CROP

    /**
     * 跳转位置，当>=0时播放前会跳转到对应位置
     * 单位：秒
     */
    var seekWhenPlay: Int = -1

    /**
     * 播放源
     */
    var source: MXPlaySource? = null

    /**
     * 是否可以通过滑动或者进度条调整进度
     */
    var canSeekByUser = true

    /**
     * 是否支持全屏
     */
    var canFullScreen = true

    /**
     * 是否显示右上角的时间
     */
    var canShowSystemTime = true

    /**
     * 是否显示右上角的电量信息
     */
    var canShowBatteryImg = true

    /**
     * 当非WiFi网络是是否弹出提示
     */
    var showTipIfNotWifi = true

    /**
     * 播放完成时如果是全屏，则退出全屏
     */
    var gotoNormalScreenWhenComplete = true

    /**
     * 播放错误时如果是全屏，则退出全屏
     */
    var gotoNormalScreenWhenError = true

    /**
     * 播放时用户可以暂停  ~~为啥需要这个？
     */
    var canPauseByUser = true

    /**
     * 播放时随着感应器旋转而全屏/小屏
     */
    var autoRotateBySensor = false

    /**
     * 是否可以快进快退
     */
    fun sourceCanSeek(): Boolean {
        return canSeekByUser && (source?.isLiveSource != true)
    }

    /**
     * 全屏时是否变更屏幕方向
     */
    fun willChangeDegreeWhenFullScreen(): Boolean {
        if (source?.changeDegreeWhenFullScreen == true) {
            return true
        }
        return videoWidth > videoHeight
    }

    /**
     * 监听器列表
     */
    val videoListeners = ArrayList<MXVideoListener>()

    fun cloneBy(target: MXConfig) {
        degree = target.degree
        videoWidth = target.videoWidth
        videoHeight = target.videoHeight
        scale = target.scale
        seekWhenPlay = target.seekWhenPlay
        source = target.source?.clone()
        canSeekByUser = target.canSeekByUser
        canFullScreen = target.canFullScreen
        canShowSystemTime = target.canShowSystemTime
        canShowBatteryImg = target.canShowBatteryImg
        showTipIfNotWifi = target.showTipIfNotWifi
        gotoNormalScreenWhenComplete = target.gotoNormalScreenWhenComplete
        gotoNormalScreenWhenError = target.gotoNormalScreenWhenError
        canPauseByUser = target.canPauseByUser
        autoRotateBySensor = target.autoRotateBySensor
    }

    fun reset() {
        degree = MXDegree.DEGREE_0
        videoWidth = 16
        videoHeight = 9
        scale = MXScale.CENTER_CROP
        seekWhenPlay = -1
        source = null
        canSeekByUser = true
        canFullScreen = true
        canShowSystemTime = true
        canShowBatteryImg = true
        showTipIfNotWifi = true
        gotoNormalScreenWhenComplete = true
        gotoNormalScreenWhenError = true
        canPauseByUser = true
    }

    fun release() {
        videoListeners.clear()
    }
}