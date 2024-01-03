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
    val title: String? = "",

    /**
     * 请求头部
     */
    val headerMap: MutableMap<String, String> = HashMap(),

    /**
     * 是否循环播放，默认 = false
     */
    val isLooping: Boolean = false,

    /**
     * 是否存储、读取播放进度
     */
    val enableSaveProgress: Boolean = false,

    /**
     * 是否直播源，当时直播时，不显示进度，无法快进快退暂停
     * 默认 = false
     */
    val isLiveSource: Boolean = false
) : Serializable {
    fun clone(): MXPlaySource {
        return MXPlaySource(
            playUri,
            title,
            headerMap,
            isLooping,
            enableSaveProgress,
            isLiveSource
        )
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MXPlaySource

        if (playUri != other.playUri) return false
        if (title != other.title) return false
        if (headerMap != other.headerMap) return false
        if (isLooping != other.isLooping) return false
        if (enableSaveProgress != other.enableSaveProgress) return false
        if (isLiveSource != other.isLiveSource) return false

        return true
    }

    override fun hashCode(): Int {
        var result = playUri.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + headerMap.hashCode()
        result = 31 * result + isLooping.hashCode()
        result = 31 * result + enableSaveProgress.hashCode()
        result = 31 * result + isLiveSource.hashCode()
        return result
    }

    override fun toString(): String {
        return "MXPlaySource(playUri=$playUri, title=$title, headerMap=$headerMap, isLooping=$isLooping, enableSaveProgress=$enableSaveProgress, isLiveSource=$isLiveSource)"
    }
}