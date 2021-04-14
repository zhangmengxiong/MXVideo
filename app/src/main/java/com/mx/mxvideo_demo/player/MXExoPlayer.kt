package com.mx.mxvideo_demo.player

import android.graphics.SurfaceTexture
import android.os.Looper
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.video.VideoListener
import com.mx.video.MXPlaySource
import com.mx.video.player.IMXPlayer
import com.mx.video.utils.MXUtils
import kotlin.math.max

class MXExoPlayer : IMXPlayer(), VideoListener, Player.EventListener {
    var mediaPlayer: SimpleExoPlayer? = null
    var mPlaySource: MXPlaySource? = null
    override fun start() {
        postInThread { mediaPlayer?.play() }
    }

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
        postInThread {
            if (!isActive()) return@postInThread
            val player = SimpleExoPlayer.Builder(context).setLooper(Looper.getMainLooper()).build()
            val sourceFactory = DefaultDataSourceFactory(context)

            val currUrl = source.playUri.toString()
            val videoSource: MediaSource = if (currUrl.contains(".m3u8")) {
                HlsMediaSource.Factory(sourceFactory)
                    .createMediaSource(MediaItem.fromUri(source.playUri))
            } else {
                ProgressiveMediaSource.Factory(sourceFactory)
                    .createMediaSource(MediaItem.fromUri(source.playUri))
            }
            player.setThrowsWhenUsingWrongThread(false)
            player.addVideoListener(this)
            player.addListener(this)
            if (source.isLooping) {
                player.repeatMode = Player.REPEAT_MODE_ONE
            } else {
                player.repeatMode = Player.REPEAT_MODE_OFF
            }
            player.setMediaSource(videoSource)
            player.playWhenReady = false
            player.setVideoSurface(Surface(surface))
            player.prepare()
            this.mediaPlayer = player
        }
    }

    override fun pause() {
        if (!isActive()) return
        postInThread { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!isActive()) return false
        return mediaPlayer?.isPlaying ?: false
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override fun seekTo(time: Int) {
        if (!isActive()) return
        postInThread {
            val duration = getDuration()
            if (duration != 0 && time >= duration) {
                // 如果直接跳转到结束位置，则直接complete
                releaseNow()
                getMXVideo()?.onPlayerCompletion()
                return@postInThread
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
        mediaPlayer?.setVideoSurface(null)
        mediaPlayer?.release()
        quitHandler()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition?.div(1000)?.toInt() ?: 0
    }

    override fun getDuration(): Int {
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return (duration / 1000).toInt()
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (!isActive()) return
        mediaPlayer?.volume = max(leftVolume, rightVolume)
    }

    override fun setSpeed(speed: Float) {
        if (!isActive()) return
        val playbackParameters = PlaybackParameters(speed, 1.0f)
        mediaPlayer?.setPlaybackParameters(playbackParameters)
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (!isActive()) return
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface
            prepare()
        } else {
            mTextureView?.surfaceTexture = mSurfaceTexture
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        MXUtils.log("onIsPlayingChanged:$isPlaying")
        if (isPlaying) {
            postInMainThread { getMXVideo()?.onPlayerBuffering(false) }
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        MXUtils.log("onPlaybackStateChanged:$state")
        when (state) {
            Player.STATE_IDLE -> {

            }
            Player.STATE_BUFFERING -> {
                postInMainThread { getMXVideo()?.onPlayerBuffering(true) }
            }
            Player.STATE_READY -> {
                postInMainThread { getMXVideo()?.onPlayerPrepared() }
            }
            Player.STATE_ENDED -> {
                postInMainThread { getMXVideo()?.onPlayerCompletion() }
            }
            else -> {
            }
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        if (!isActive()) return
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK -> {
                postInMainThread { getMXVideo()?.onPlayerSeekComplete() }
            }
            else -> {
            }
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        if (!isActive()) return
        postInMainThread {
            getMXVideo()?.onPlayerError(error.message)
            release()
        }
    }

    override fun onRenderedFirstFrame() {
        postInMainThread { getMXVideo()?.onPlayerStartPlay() }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {

    }

    override fun onVideoSizeChanged(
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerVideoSizeChanged(width, height) }
    }
}