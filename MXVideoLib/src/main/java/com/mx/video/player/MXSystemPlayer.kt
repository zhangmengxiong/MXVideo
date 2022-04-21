package com.mx.video.player

import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.view.Surface
import com.mx.video.beans.MXPlaySource

class MXSystemPlayer : IMXPlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnVideoSizeChangedListener {
    var mediaPlayer: MediaPlayer? = null
    var mPlaySource: MXPlaySource? = null

    override fun setSource(source: MXPlaySource) {
        mPlaySource = source
    }

    override fun prepare() {
        if (!isActive()) return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
        val context = context ?: return
        releaseNow()
        initHandler()
        postInThread {
            if (!isActive()) return@postInThread

            val mediaPlayer = MediaPlayer()
            this.mediaPlayer = mediaPlayer

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val build = AudioAttributes.Builder()
                build.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                mediaPlayer.setAudioAttributes(build.build())
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
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

    override fun enablePreload(): Boolean {
        return true
    }

    override fun start() {
        if (!isActive()) return
        notifyStartPlay()
        postInThread { mediaPlayer?.start() }
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
        val source = mPlaySource ?: return
        if (!isActive() || source.isLiveSource) return
        val duration = getDuration()
        if (duration != 0 && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            notifyPlayerCompletion()
            return
        }

        postInThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo(time * 1000L, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(time * 1000)
            }
            notifyBuffering(true)
        }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少
        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        mSurfaceTexture = null

        mediaPlayer?.setSurface(null)
        postInThread {
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
        if (!isActive()) return 0
        return mediaPlayer?.currentPosition?.div(1000) ?: 0
    }

    override fun getDuration(): Int {
        if (!isActive()) return 0
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
        val mediaPlayer = mediaPlayer ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pp = mediaPlayer.playbackParams ?: PlaybackParams()
            pp.speed = speed
            mediaPlayer.playbackParams = pp
        }
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

    override fun onPrepared(mp: MediaPlayer?) {
        if (!isActive()) return
        notifyPrepared()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (!isActive()) return
        notifyPlayerCompletion()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (!isActive()) return
        notifyBufferingUpdate(percent)
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        if (!isActive()) return
        notifyBuffering(false)
        notifySeekComplete()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        notifyError("what = $what    extra = $extra")
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                notifyStartPlay()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                notifyBuffering(true)
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                notifyBuffering(false)
            }
            else -> {
                onPlayerInfo("what = $what    extra = $extra")
            }
        }
        return true
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        if (!isActive()) return
        notifyVideoSize(width, height)
    }
}