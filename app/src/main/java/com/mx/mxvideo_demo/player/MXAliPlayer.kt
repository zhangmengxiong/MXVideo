package com.mx.mxvideo_demo.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.Surface
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoBean
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.source.UrlSource
import com.mx.video.beans.MXPlaySource
import com.mx.video.player.IMXPlayer


class MXAliPlayer : IMXPlayer(), IPlayer.OnPreparedListener, IPlayer.OnCompletionListener,
    IPlayer.OnSeekCompleteListener, IPlayer.OnErrorListener, IPlayer.OnInfoListener,
    IPlayer.OnVideoSizeChangedListener, IPlayer.OnLoadingStatusListener,
    IPlayer.OnStateChangedListener {
    var mediaPlayer: AliPlayer? = null

    override fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        postInThread {
            val mediaPlayer = AliPlayerFactory.createAliPlayer(context)
            this.mediaPlayer = mediaPlayer

            mediaPlayer.setTraceId("DisableAnalytics") // 禁止日志埋点上报
            mediaPlayer.isLoop = false
            mediaPlayer.isAutoPlay = false
            mediaPlayer.setOnPreparedListener(this@MXAliPlayer)
            mediaPlayer.setOnCompletionListener(this@MXAliPlayer)
            mediaPlayer.setOnSeekCompleteListener(this@MXAliPlayer)
            mediaPlayer.setOnErrorListener(this@MXAliPlayer)
            mediaPlayer.setOnInfoListener(this@MXAliPlayer)
            mediaPlayer.setOnVideoSizeChangedListener(this@MXAliPlayer)
            mediaPlayer.setOnLoadingStatusListener(this@MXAliPlayer)
            mediaPlayer.setOnStateChangedListener(this@MXAliPlayer)

            mediaPlayer.scaleMode = IPlayer.ScaleMode.SCALE_TO_FILL
            mediaPlayer.rotateMode = IPlayer.RotateMode.ROTATE_0
            mediaPlayer.mirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE


            mediaPlayer.setDataSource(UrlSource().apply {
                this.uri = source.playUri.toString()
            })
            mediaPlayer.prepare()
            mediaPlayer.setSurface(Surface(surface))
        }
    }

    override fun enablePreload(): Boolean {
        return true
    }

    override fun start() {
        if (!active) return
        postInThread { mediaPlayer?.start() }
        notifyStartPlay()
        postBuffering()
    }

    override fun pause() {
        if (!active) return
        postInThread { mediaPlayer?.pause() }
    }

    private var currentState = IPlayer.idle
    override fun isPlaying(): Boolean {
        if (!active) return false
        return currentState in arrayOf(IPlayer.started, IPlayer.stopped)
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override fun seekTo(time: Int) {
        val source = source ?: return
        if (!active || source.isLiveSource) return
        val duration = getDuration()
        if (duration != 0 && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            notifyPlayerCompletion()
            return
        }

        postInThread {
            currentPosition = time
            mediaPlayer?.seekTo(time * 1000L)
        }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        try {
            mediaPlayer?.setSurface(null)
            mediaPlayer?.release()
        } catch (e: Exception) {
        }
    }

    private var currentPosition = 0
    override fun getPosition(): Int {
        if (!active) return 0
        return currentPosition
    }

    override fun getDuration(): Int {
        if (!active) return 0
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return (duration / 1000).toInt()
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        mediaPlayer?.volume = leftVolume
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        mediaPlayer?.speed = speed
    }

    override fun onPrepared() {
        if (!active) return
        notifyPrepared()
    }

    override fun onCompletion() {
        if (!active) return
        notifyPlayerCompletion()
    }

    override fun onSeekComplete() {
        if (!active) return
        notifySeekComplete()
    }

    override fun onError(p0: ErrorInfo?) {
        if (!active) return
        notifyError("what = ${p0.toString()}")
    }

    override fun onInfo(infoBean: InfoBean) {
        if (infoBean.code == InfoCode.CurrentPosition) {
            //extraValue为当前播放进度，单位为毫秒
            currentPosition = (infoBean.extraValue / 1000).toInt()
        }
    }

    override fun onVideoSizeChanged(p0: Int, p1: Int) {
        if (!active) return
        notifyVideoSize(p0, p1)
    }

    override fun onLoadingBegin() {
        notifyBuffering(true)
    }

    override fun onLoadingProgress(p0: Int, p1: Float) {
        if (!active) return
        notifyBufferingUpdate(p0)
    }

    override fun onLoadingEnd() {
        notifyBuffering(false)
    }

    override fun onStateChanged(newState: Int) {
        currentState = newState
    }
}