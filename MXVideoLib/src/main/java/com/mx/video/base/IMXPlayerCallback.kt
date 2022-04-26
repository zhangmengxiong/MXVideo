package com.mx.video.base

interface IMXPlayerCallback {
    /**
     * 播放器回调：准备完成
     */
    fun onPlayerPrepared()

    /**
     * 播放器回调：开始播放
     */
    fun onPlayerStartPlay()

    /**
     * 播放器回调：播放完成
     */
    fun onPlayerCompletion()

    /**
     * 播放器回调：缓冲进度
     */
    fun onPlayerBufferProgress(percent: Int)

    /**
     * 播放器回调：快进快退完成
     */
    fun onPlayerSeekComplete()

    /**
     * 播放器回调：播放错误
     */
    fun onPlayerError(error: String?)

    /**
     * 播放器回调：缓冲状态
     * @param start
     *      是否正在缓冲中
     */
    fun onPlayerBuffering(start: Boolean)

    /**
     * 播放器回调：获取视频宽高
     */
    fun onPlayerVideoSizeChanged(width: Int, height: Int)

    /**
     * 播放器回调：日志输出
     */
    fun onPlayerInfo(message: String?)
}