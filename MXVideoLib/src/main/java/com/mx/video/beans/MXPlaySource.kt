package com.mx.video.beans

import android.net.Uri
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
     * 全屏时是否需要变更Activity方向
     * 如果 = null，会自动根据视频宽高来判断
     * 默认 = null
     */
    val changeOrientationWhenFullScreen: Boolean? = null,

    /**
     * 是否循环播放，默认 = false
     */
    val isLooping: Boolean = false,

    /**
     * 是否存储、读取播放进度
     */
    val enableSaveProgress: Boolean = true,

    /**
     * 是否直播源，当时直播时，不显示进度，无法快进快退暂停
     * 默认 = false
     */
    val isLiveSource: Boolean = false,
) : Serializable {
    fun clone(): MXPlaySource {
        return MXPlaySource(
            playUri,
            title,
            headerMap,
            changeOrientationWhenFullScreen,
            isLooping,
            enableSaveProgress,
            isLiveSource
        )
    }
}