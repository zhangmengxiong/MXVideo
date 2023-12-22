package com.mx.video.base

import com.mx.video.beans.MXPlaySource

interface IMXPlayerCallback {
    /**
     * 播放器回调：准备完成
     */
    suspend fun onPlayerPrepared()

    /**
     * 播放器回调：开始播放
     */
    suspend fun onPlayerStartPlay()

    /**
     * 播放器回调：播放完成
     */
    suspend fun onPlayerCompletion()

    /**
     * 播放器回调：缓冲进度
     */
    suspend fun onPlayerBufferProgress(percent: Int)

    /**
     * 播放器回调：快进快退完成
     */
    suspend fun onPlayerSeekComplete()

    /**
     * 播放器回调：播放错误
     */
    suspend fun onPlayerError(source: MXPlaySource, error: String)

    /**
     * 播放器回调：缓冲状态
     * @param start
     *      是否正在缓冲中
     */
    suspend fun onPlayerBuffering(start: Boolean)

    /**
     * 播放器回调：获取视频宽高
     */
    suspend fun onPlayerVideoSizeChanged(width: Int, height: Int)

    /**
     * 播放器回调：日志输出
     */
    suspend fun onPlayerInfo(message: String?)
}