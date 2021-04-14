package com.mx.video

import android.net.Uri
import android.view.ViewGroup
import java.io.Serializable

/**
 * 播放源枚举
 */
data class MXPlaySource(
    /**
     * 播放源
     */
    val playUri: Uri,

    /**
     * 标题
     */
    val title: String = "",

    /**
     * 请求头部
     */
    val headerMap: MutableMap<String, String> = HashMap(),

    /**
     * 是否在线资源，播放本地文件时需要定义为false，网络资源定义为true
     * 默认 = true
     */
    val isOnlineSource: Boolean = true,

    /**
     * 全屏时是否需要变更Activity方向
     * 如果 = null，会自动根据视频宽高来判断
     * 默认 = null
     */
    val canChangeOrientationIfFullScreen: Boolean? = null,

    /**
     * 是否循环播放，默认 = false
     */
    val isLooping: Boolean = false,

    /**
     * 是否存储、读取播放进度
     */
    val enableSaveProgress: Boolean = true

) : Serializable {
    fun clone(): MXPlaySource {
        return MXPlaySource(
            playUri,
            title,
            headerMap,
            isOnlineSource,
            canChangeOrientationIfFullScreen,
            isLooping,
            enableSaveProgress
        )
    }
}

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

/**
 * 播放缩放类型枚举
 */
enum class MXScale : Serializable {
    FILL_PARENT, // 填充宽高
    CENTER_CROP // 根据视频宽高自适应
}

/**
 * 播放屏幕类型枚举
 */
enum class MXScreen : Serializable {
    FULL, // 全屏
    NORMAL  // 小屏
}

/**
 * 全屏父类封装
 */
data class MXParentView(
    val index: Int,
    val parentViewGroup: ViewGroup,
    val layoutParams: ViewGroup.LayoutParams,
    val width: Int,
    val height: Int
) : Serializable

/**
 * 播放属性配置
 */
class MXConfig : Serializable {
    var canSeekByUser = true // 是否可以通过滑动或者进度条调整进度
    var canFullScreen = true // 是否支持全屏
    var canShowSystemTime = true // 是否显示右上角的时间
    var canShowBatteryImg = true // 是否显示右上角的电量信息
    var showTipIfNotWifi = true // 当非WiFi网络是是否弹出提示
    var gotoNormalScreenWhenComplete = true // 播放完成时如果是全屏，则退出全屏
    var gotoNormalScreenWhenError = true // 播放错误时如果是全屏，则退出全屏
    var canPauseByUser = true // 播放时用户可以暂停  ~~为啥需要这个？

    fun cloneBy(target: MXConfig) {
        canSeekByUser = target.canSeekByUser
        canFullScreen = target.canFullScreen
        canShowSystemTime = target.canShowSystemTime
        canShowBatteryImg = target.canShowBatteryImg
        showTipIfNotWifi = target.showTipIfNotWifi
        gotoNormalScreenWhenComplete = target.gotoNormalScreenWhenComplete
        gotoNormalScreenWhenError = target.gotoNormalScreenWhenError
        canPauseByUser = target.canPauseByUser
    }
}