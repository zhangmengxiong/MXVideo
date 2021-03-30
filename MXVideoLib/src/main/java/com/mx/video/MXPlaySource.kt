package com.mx.video

data class MXPlaySource(
    val playUrl: String, // 播放源
    val title: String = "", // 标题
    val headerMap: HashMap<String, String>, // 请求头部
    val isOnlineSource: Boolean = true, // 是否在线资源
    val isLooping: Boolean = false // 是否循环播放
)

enum class MXPlayState {
    IDLE,
    NORMAL,
    PREPARING,
    PREPARING_CHANGE_URL,
    PREPARING_PLAYING,
    PREPARED,
    PLAYING,
    PAUSE,
    COMPLETE,
    ERROR
}

enum class MXVideoDisplay {
    FILL_PARENT,
    ORIGINAL,
    FILL_SCROP
}