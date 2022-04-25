package com.mx.video.base

import com.mx.video.beans.*
import com.mx.video.player.IMXPlayer

interface IMXVideo {
    /**
     * 设置播放器
     */
    fun setPlayer(player: Class<out IMXPlayer>? = null)

    /**
     * 设置播放源
     */
    fun setSource(source: MXPlaySource?, seekTo: Int = -1)

    /**
     * 设置渲染旋转方向
     */
    fun setTextureOrientation(orientation: MXOrientation)

    /**
     * 播放跳转
     * @param seek 单位：秒
     */
    fun seekTo(seek: Int)

    /**
     * 设置播放器画面渲染填充类型
     * @param type
     *      CENTER_CROP = 根据视频宽高等比缩放
     *      FILL_PARENT = 拉伸填充播放器View的宽高
     *      默认 = CENTER_CROP
     */
    fun setScaleType(type: MXScale)

    /**
     * 设置MXVideo的宽高比，设置之后会自动计算播放器的高度
     * @param ratio 宽/高
     */
    fun setDimensionRatio(ratio: Double)

    /**
     * 设置静音
     */
    fun setAudioMute(mute: Boolean)

    /**
     * 开始播放
     */
    fun startPlay()

    /**
     * 开始预加载
     */
    fun startPreload()

    /**
     * 停止播放
     */
    fun stopPlay()

    /**
     * 暂停播放
     */
    fun pausePlay()

    /**
     * 恢复播放
     */
    fun continuePlay()

    /**
     * 是否播放中
     */
    fun isPlaying(): Boolean

    /**
     * 获取视频总时长
     * 单位：秒
     */
    fun getDuration(): Int

    /**
     * 获取当前播放进度
     * 单位：秒
     */
    fun getPosition(): Int

    /**
     * 切换播放器是否全屏
     */
    fun switchToScreen(screen: MXScreen): Boolean

    /**
     * 获取当前全屏状态
     */
    fun currentScreen(): MXScreen

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

    /**
     * Activity/Fragment 生命周期onStart() 需要调用暂停
     */
    fun onStart()

    /**
     * Activity/Fragment 生命周期onStop() 需要调用暂停
     */
    fun onStop()

    /**
     * Activity/Fragment 生命周期onDestroy() 释放资源
     */
    fun release()
}