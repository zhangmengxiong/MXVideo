package com.mx.video

import android.net.Uri
import android.view.ViewGroup
import java.io.Serializable

data class MXPlaySource(
    val playUri: Uri, // 播放源
    val title: String = "", // 标题
    val headerMap: MutableMap<String, String> = HashMap(), // 请求头部
    val isOnlineSource: Boolean = true, // 是否在线资源
    val canChangeOrientationIfFullScreen: Boolean? = null, // 全屏时是否需要变更Activity方向,如果=null，会自动根据视频宽高来判断
    val isLooping: Boolean = false // 是否循环播放
) : Serializable {
    fun clone(): MXPlaySource {
        return MXPlaySource(
            playUri,
            title,
            headerMap,
            isOnlineSource,
            canChangeOrientationIfFullScreen,
            isLooping
        )
    }
}

enum class MXState : Serializable {
    IDLE, // 初始状态
    NORMAL, // 已设置数据源，但未开始
    PREPARING, // 视频资源校验准备中
    PREPARED, // 视频资源校验完成
    PLAYING, // 播放中
    PAUSE, // 暂停中
    COMPLETE, // 播放完成
    ERROR // 播放错误
}

enum class MXScale : Serializable {
    FILL_PARENT, // 填充宽高
    CENTER_CROP // 根据视频宽高自适应
}

enum class MXScreen : Serializable {
    FULL, // 全屏
    NORMAL  // 小屏
}

data class MXParentView(
    val index: Int,
    val parentViewGroup: ViewGroup,
    val layoutParams: ViewGroup.LayoutParams,
    val width: Int,
    val height: Int
) : Serializable

class MXConfig : Serializable {
    var canSeekByUser = true // 是否可以通过滑动或者进度条调整进度
    var canFullScreen = true // 是否支持全屏
    var canShowSystemTime = true // 是否显示右上角的时间
    var canShowBatteryImg = true // 是否显示右上角的电量信息
    var showTipIfNotWifi = true // 当非WiFi网络是是否弹出提示
    var gotoNormalScreenWhenComplete = true // 播放完成时如果是全屏，则退出全屏
    var gotoNormalScreenWhenError = true // 播放错误时如果是全屏，则退出全屏

    fun cloneBy(target: MXConfig) {
        canSeekByUser = target.canSeekByUser
        canFullScreen = target.canFullScreen
        canShowSystemTime = target.canShowSystemTime
        canShowBatteryImg = target.canShowBatteryImg
        showTipIfNotWifi = target.showTipIfNotWifi
        gotoNormalScreenWhenComplete = target.gotoNormalScreenWhenComplete
        gotoNormalScreenWhenError = target.gotoNormalScreenWhenError
    }
}