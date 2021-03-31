package com.mx.video

data class MXPlaySource(
    val playUrl: String, // 播放源
    val title: String = "", // 标题
    val headerMap: MutableMap<String, String> = HashMap(), // 请求头部
    val isOnlineSource: Boolean = true, // 是否在线资源
    val isLooping: Boolean = false // 是否循环播放
)

enum class MXPlayState {
    IDLE,
    NORMAL,
    PREPARING,
    PREPARED,
    PLAYING,
    PAUSE,
    COMPLETE,
    ERROR
}

enum class MXVideoDisplay {
    FILL_PARENT, // 填充宽高
    CENTER_CROP // 根据视频宽高自适应
}