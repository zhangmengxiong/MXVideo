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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MXAliPlayer : IMXPlayer(), IPlayer.OnPreparedListener, IPlayer.OnCompletionListener,
    IPlayer.OnSeekCompleteListener, IPlayer.OnErrorListener, IPlayer.OnInfoListener,
    IPlayer.OnVideoSizeChangedListener, IPlayer.OnLoadingStatusListener,
    IPlayer.OnStateChangedListener {
    private var mediaPlayer: AliPlayer? = null

    override suspend fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        val mediaPlayer = AliPlayerFactory.createAliPlayer(context)
        this@MXAliPlayer.mediaPlayer = mediaPlayer

        mediaPlayer.setOnPreparedListener(this@MXAliPlayer)
        mediaPlayer.setOnCompletionListener(this@MXAliPlayer)
        mediaPlayer.setOnSeekCompleteListener(this@MXAliPlayer)
        mediaPlayer.setOnErrorListener(this@MXAliPlayer)
        mediaPlayer.setOnInfoListener(this@MXAliPlayer)
        mediaPlayer.setOnVideoSizeChangedListener(this@MXAliPlayer)
        mediaPlayer.setOnLoadingStatusListener(this@MXAliPlayer)
        mediaPlayer.setOnStateChangedListener(this@MXAliPlayer)

        mediaPlayer.isLoop = false
        mediaPlayer.isAutoPlay = false
        mediaPlayer.scaleMode = IPlayer.ScaleMode.SCALE_TO_FILL
        mediaPlayer.rotateMode = IPlayer.RotateMode.ROTATE_0
        mediaPlayer.mirrorMode = IPlayer.MirrorMode.MIRROR_MODE_NONE

        mediaPlayer.setDataSource(UrlSource().apply {
            this.uri = source.playUri.toString()
        })
        mediaPlayer.setSurface(Surface(surface))
        withContext(Dispatchers.IO) { mediaPlayer.prepare() }
    }

    override fun enablePreload(): Boolean {
        return true
    }

    override suspend fun start() {
        if (!active) return
        withContext(Dispatchers.IO) {
            mediaPlayer?.start()
        }
        notifyStartPlay()
        postBuffering()
    }

    override suspend fun pause() {
        if (!active) return
        withContext(Dispatchers.IO) { mediaPlayer?.pause() }
    }

    private var currentState = IPlayer.idle
    override fun isPlaying(): Boolean {
        if (!active) return false
        return currentState in arrayOf(IPlayer.started, IPlayer.stopped)
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override suspend fun seekTo(time: Int) = withContext(Dispatchers.IO) {
        val source = source ?: return@withContext
        if (!active || source.isLiveSource) return@withContext

        val duration = getDuration()
        if (duration != 0f && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            notifyPlayerCompletion()
            return@withContext
        }
        mediaPlayer?.seekTo(time * 1000L, IPlayer.SeekMode.Accurate)
        currentPosition = time.toFloat()
    }

    override suspend fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        try {
            mediaPlayer?.setSurface(null)
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
    }

    private var currentPosition = 0f
    override fun getPosition(): Float {
        if (!active) return 0f
        return currentPosition
    }

    override fun getDuration(): Float {
        if (!active) return 0f
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return duration / 1000f
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        mediaPlayer?.volume = (leftVolume + rightVolume) / 2f
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        mediaPlayer?.speed = speed
    }

    override fun onPrepared() {
        launch { notifyPrepared() }
    }

    override fun onCompletion() {
        launch { notifyPlayerCompletion() }
    }

    override fun onSeekComplete() {
        launch { notifySeekComplete() }
    }

    override fun onError(info: ErrorInfo?) {
        launch { notifyError("code = ${info?.code?.name}  msg = ${info?.msg}  extra = ${info?.extra}") }
    }

    override fun onInfo(infoBean: InfoBean) {
        if (infoBean.code == InfoCode.CurrentPosition) {
            currentPosition = infoBean.extraValue / 1000f
        }
    }

    override fun onVideoSizeChanged(width: Int, height: Int) {
        launch { notifyVideoSize(width, height) }
    }

    override fun onLoadingBegin() {
        launch { notifyBuffering(true) }
    }

    override fun onLoadingProgress(progress: Int, netSpeed: Float) {
        launch { notifyBufferingUpdate(progress) }
    }

    override fun onLoadingEnd() {
        launch { notifyBuffering(false) }
    }

    override fun onStateChanged(newState: Int) {
        currentState = newState
    }
}