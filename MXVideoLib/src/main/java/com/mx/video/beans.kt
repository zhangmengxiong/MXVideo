package com.mx.video

import android.view.ViewGroup

data class MXPlaySource(
    val playUrl: String, // 播放源
    val title: String = "", // 标题
    val headerMap: MutableMap<String, String> = HashMap(), // 请求头部
    val isOnlineSource: Boolean = true, // 是否在线资源
    val isLooping: Boolean = false // 是否循环播放
)

enum class MXState {
    IDLE,
    NORMAL,
    PREPARING,
    PREPARED,
    PLAYING,
    PAUSE,
    COMPLETE,
    ERROR
}

enum class MXScale {
    FILL_PARENT, // 填充宽高
    CENTER_CROP // 根据视频宽高自适应
}

enum class MXScreen {
    FULL, // 全屏
    NORMAL  // 小屏
}

data class MXParentView(
    val index: Int,
    val parentViewGroup: ViewGroup,
    val layoutParams: ViewGroup.LayoutParams,
    val width: Int,
    val height: Int
)

class MXConfig {
    var canSeekByUser = true // 是否可以通过滑动或者进度条调整进度
    var canFullScreen = true // 是否支持全屏
    var canShowSystemTime = true // 是否显示右上角的时间
    var canShowBatteryImg = true // 是否显示右上角的电量信息
    var showTipIfNotWifi = false // 当非WiFi网络是是否弹出提示
    var gotoNormalScreenWhenComplete = true // 播放完成时如果是全屏，则退出全屏
    var gotoNormalScreenWhenError = true // 播放错误时如果是全屏，则退出全屏
}