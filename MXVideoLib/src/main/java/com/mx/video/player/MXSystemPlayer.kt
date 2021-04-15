package com.mx.video.player

import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import com.mx.video.beans.MXPlaySource

class MXSystemPlayer : IMXPlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnVideoSizeChangedListener {
    var mediaPlayer: MediaPlayer? = null
    var mPlaySource: MXPlaySource? = null
    override fun start() {
        postInThread { mediaPlayer?.start() }
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

            val mediaPlayer = MediaPlayer()
            this.mediaPlayer = mediaPlayer

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.isLooping = source.isLooping
            mediaPlayer.setOnPreparedListener(this@MXSystemPlayer)
            mediaPlayer.setOnCompletionListener(this@MXSystemPlayer)
            mediaPlayer.setOnBufferingUpdateListener(this@MXSystemPlayer)
            mediaPlayer.setScreenOnWhilePlaying(true)
            mediaPlayer.setOnSeekCompleteListener(this@MXSystemPlayer)
            mediaPlayer.setOnErrorListener(this@MXSystemPlayer)
            mediaPlayer.setOnInfoListener(this@MXSystemPlayer)
            mediaPlayer.setOnVideoSizeChangedListener(this@MXSystemPlayer)

            mediaPlayer.setDataSource(context, source.playUri, source.headerMap)

            mediaPlayer.prepareAsync()
            mediaPlayer.setSurface(Surface(surface))
        }
    }

    override fun pause() {
        if (!isActive()) return
        postInThread { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!isActive()) return false
        if (mediaPlayer?.isPlaying == true) {
            return true
        }
        return false
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override fun seekTo(time: Int) {
        if (!isActive()) return
        val duration = getDuration()
        if (duration != 0 && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            releaseNow()
            getMXVideo()?.onPlayerCompletion()
            return
        }

        postInThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo(time * 1000L, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(time * 1000)
            }
        }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        mSurfaceTexture = null

        postInThread {
            mediaPlayer?.setSurface(null)
            mediaPlayer?.release()
            quitHandler()
        }
    }

    private fun releaseNow() {
        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        mediaPlayer?.setSurface(null)
        mediaPlayer?.release()
        quitHandler()
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition?.div(1000) ?: 0
    }

    override fun getDuration(): Int {
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return duration / 1000
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (!isActive()) return
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun setSpeed(speed: Float) {
        if (!isActive()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pp = mediaPlayer?.playbackParams ?: return
            pp.speed = speed
            mediaPlayer?.playbackParams = pp
        }
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

    override fun onPrepared(mp: MediaPlayer?) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerPrepared() }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerCompletion() }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerBufferProgress(percent) }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerSeekComplete() }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        postInMainThread {
            getMXVideo()?.onPlayerError("what = $what    extra = $extra")
            release()
        }
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                postInMainThread { getMXVideo()?.onPlayerStartPlay() }
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                postInMainThread { getMXVideo()?.onPlayerBuffering(true) }
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                postInMainThread { getMXVideo()?.onPlayerBuffering(false) }
            }
        }
        return true
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        if (!isActive()) return
        postInMainThread { getMXVideo()?.onPlayerVideoSizeChanged(width, height) }
    }
}