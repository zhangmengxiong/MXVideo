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
    SMALL  // 小屏
}

data class MXParentView(
    val index: Int,
    val parentViewGroup: ViewGroup,
    val layoutParams: ViewGroup.LayoutParams
)