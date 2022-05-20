package com.mx.mxvideo_demo.player.exo

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Looper
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.video.VideoSize
import com.mx.video.beans.MXPlaySource
import com.mx.video.player.IMXPlayer

class MXExoPlayer : IMXPlayer(), Player.Listener, Player.EventListener, AnalyticsListener {
    private var mediaPlayer: ExoPlayer? = null

    override fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        postInMainThread {
            isBuffering = false
            isPreparedCall = false
            isStartPlayCall = false

            val player = ExoPlayer.Builder(context, DefaultRenderersFactory(context))
                .setLooper(Looper.getMainLooper())
                .setTrackSelector(DefaultTrackSelector(context))
                .setLoadControl(DefaultLoadControl()).build()
            this.mediaPlayer = player
            val currUrl = source.playUri.toString()

            val build = AudioAttributes.Builder()
            build.setUsage(C.USAGE_MEDIA)
            build.setContentType(C.CONTENT_TYPE_MUSIC)
            player.setAudioAttributes(build.build(), false)

            player.addListener(this)
            player.addAnalyticsListener(this)
            player.repeatMode = Player.REPEAT_MODE_OFF
            player.setMediaSource(
                ExoSourceBuild.build(context, source.headerMap, currUrl, false)
            )
            player.playWhenReady = false
            player.setVideoSurface(Surface(surface))
            player.prepare()
        }
    }

    override fun enablePreload(): Boolean {
        return true
    }

    override fun start() {
        postInMainThread { mediaPlayer?.play() }
        notifyStartPlay()
        postBuffering()
    }

    override fun pause() {
        if (!active) return
        mediaPlayer?.pause()
    }

    override fun isPlaying(): Boolean {
        if (!active) return false
        return mediaPlayer?.isPlaying ?: false
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override fun seekTo(time: Int) {
        val source = source ?: return
        if (!active || source.isLiveSource) return
        postInMainThread {
            val duration = getDuration()
            if (duration != 0 && time >= duration) {
                // 如果直接跳转到结束位置，则直接complete
                notifyPlayerCompletion()
            } else {
                mediaPlayer?.seekTo(time * 1000L)
            }
        }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null

        try {
            mediaPlayer?.setVideoSurface(null)
            mediaPlayer?.release()
        } catch (e: Exception) {
        }
    }

    override fun getPosition(): Int {
        if (!active) return 0
        return mediaPlayer?.currentPosition?.div(1000)?.toInt() ?: 0
    }

    override fun getDuration(): Int {
        if (!active) return 0
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return (duration / 1000).toInt()
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        mediaPlayer?.volume = (leftVolume + rightVolume) / 2f
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        mediaPlayer?.playbackParameters = PlaybackParameters(speed, 1.0f)
    }

    override fun onPlaybackStateChanged(state: Int) {
        if (!active) return
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
                    notifyBuffering(true)
                    isBuffering = true
                }
                Player.STATE_READY -> {
                    notifyPrepared()
                    notifyBuffering(false)
                }
                Player.STATE_ENDED -> {
                    notifyPlayerCompletion()
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
        if (!active) return
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            notifySeekComplete()
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        if (!active) return
        notifyError(error.message)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        if (!active) return
        notifyVideoSize(videoSize.width, videoSize.height)
    }
}