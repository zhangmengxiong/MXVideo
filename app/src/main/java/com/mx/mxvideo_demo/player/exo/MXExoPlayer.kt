package com.mx.mxvideo_demo.player.exo

import android.graphics.SurfaceTexture
import android.os.Looper
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.video.VideoSize
import com.mx.video.beans.MXPlaySource
import com.mx.video.player.IMXPlayer

class MXExoPlayer : IMXPlayer(), Player.Listener, Player.EventListener, AnalyticsListener {
    private var mediaPlayer: ExoPlayer? = null
    private var mPlaySource: MXPlaySource? = null

    override fun setSource(source: MXPlaySource) {
        mPlaySource = source
    }

    override fun prepare() {
        if (!isActive()) return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
        val context = getMXVideo()?.context ?: return
        releaseNow()
        initHandler()
        postInMainThread {
            if (!isActive()) return@postInMainThread

            isBuffering = false
            isPreparedCall = false
            isStartPlayCall = false

            val player = ExoPlayer.Builder(context, DefaultRenderersFactory(context))
                .setLooper(Looper.getMainLooper())
                .setTrackSelector(DefaultTrackSelector(context))
                .setLoadControl(DefaultLoadControl()).build()
            this.mediaPlayer = player
            val currUrl = source.playUri.toString()
            player.addListener(this)
            player.addAnalyticsListener(this)
            if (source.isLooping) {
                player.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                player.repeatMode = Player.REPEAT_MODE_OFF
            }
            player.setMediaSource(
                ExoSourceBuild.build(context, source.headerMap, currUrl, false)
            )
            player.playWhenReady = false
            player.setVideoSurface(Surface(surface))
            player.prepare()
        }
    }

    override fun start() {
        if (!isActive()) return
        postInMainThread { mediaPlayer?.play() }
    }

    override fun pause() {
        if (!isActive()) return
        mediaPlayer?.pause()
    }

    override fun isPlaying(): Boolean {
        if (!isActive()) return false
        return mediaPlayer?.isPlaying ?: false
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override fun seekTo(time: Int) {
        if (!isActive()) return
        postInMainThread {
            val duration = getDuration()
            if (duration != 0 && time >= duration) {
                // 如果直接跳转到结束位置，则直接complete
                releaseNow()
                getMXVideo()?.onPlayerCompletion()
                return@postInMainThread
            }
            mediaPlayer?.seekTo(time * 1000L)
        }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        mSurfaceTexture = null

        postInThread {
            mediaPlayer?.setVideoSurface(null)
            mediaPlayer?.release()
            quitHandler()
        }
    }

    private fun releaseNow() {
        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        quitHandler()
        postInThread {
            mediaPlayer?.setVideoSurface(null)
            mediaPlayer?.release()
        }
    }

    override fun getCurrentPosition(): Int {
        if (!isActive()) return 0
        return mediaPlayer?.currentPosition?.div(1000)?.toInt() ?: 0
    }

    override fun getDuration(): Int {
        if (!isActive()) return 0
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return (duration / 1000).toInt()
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (!isActive()) return
        mediaPlayer?.volume = (leftVolume + rightVolume) / 2f
    }

    override fun setSpeed(speed: Float) {
        if (!isActive()) return
        mediaPlayer?.playbackParameters = PlaybackParameters(speed, 1.0f)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!isActive()) return
        val texture = mSurfaceTexture
        if (texture == null) {
            mSurfaceTexture = surface
            prepare()
        } else {
            mTextureView?.setSurfaceTexture(texture)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (!isActive()) return
        onPlayWhenReadyChanged(isLastReportedPlayWhenReady ?: false, state)
    }

    private var isLastReportedPlayWhenReady: Boolean? = null
    private var lastReportedPlaybackState: Int? = null
    private var isBuffering = false
    private var isPreparedCall = false
    private var isStartPlayCall = false

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
        //重新播放状态顺序为：STATE_IDLE -》STATE_BUFFERING -》STATE_READY
        //缓冲时顺序为：STATE_BUFFERING -》STATE_READY
        if (isLastReportedPlayWhenReady != playWhenReady || lastReportedPlaybackState != playbackState) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    postInMainThread { getMXVideo()?.onPlayerBuffering(true) }
                    isBuffering = true
                }
                Player.STATE_READY -> {
                    postInMainThread {
                        if (!isPreparedCall) {
                            getMXVideo()?.onPlayerPrepared()
                            isPreparedCall = true
                        }
                        if (!isStartPlayCall) {
                            getMXVideo()?.onPlayerStartPlay()
                            isStartPlayCall = true
                        }
                        if (isBuffering) {
                            getMXVideo()?.onPlayerBuffering(false)
                            isBuffering = false
                        }
                    }
                }
                Player.STATE_ENDED -> {
                    postInMainThread {
                        getMXVideo()?.onPlayerCompletion()
                        release()
                    }
                }
                else -> {}
            }
        }
        isLastReportedPlayWhenReady = playWhenReady
        lastReportedPlaybackState = playbackState
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (!isActive()) return
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            postInMainThread { getMXVideo()?.onPlayerSeekComplete() }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        if (!isActive()) return
        postInMainThread {
            getMXVideo()?.onPlayerError(error.message)
            release()
        }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        if (!isActive()) return
        postInMainThread {
            getMXVideo()?.onPlayerVideoSizeChanged(
                videoSize.width,
                videoSize.height
            )
        }
    }
}