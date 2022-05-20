package com.mx.video.player

import android.content.Context
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

    override fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        postInThread {
            val mediaPlayer = MediaPlayer()
            this.mediaPlayer = mediaPlayer

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val build = AudioAttributes.Builder()
                build.setUsage(AudioAttributes.USAGE_MEDIA)
                build.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                mediaPlayer.setAudioAttributes(build.build())
            } else {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            mediaPlayer.isLooping = false
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
        if (!active) return
        postInThread { mediaPlayer?.start() }
        notifyStartPlay()
        postBuffering()
    }

    override fun pause() {
        if (!active) return
        postInThread { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!active) return false
        if (mediaPlayer?.isPlaying == true) {
            return true
        }
        return false
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo(time * 1000L, MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(time * 1000)
            }
//            notifyBuffering(true)
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

    override fun getPosition(): Int {
        if (!active) return 0
        return mediaPlayer?.currentPosition?.div(1000) ?: 0
    }

    override fun getDuration(): Int {
        if (!active) return 0
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return duration / 1000
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        val mediaPlayer = mediaPlayer ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pp = mediaPlayer.playbackParams ?: PlaybackParams()
            pp.speed = speed
            mediaPlayer.playbackParams = pp
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (!active) return
        notifyPrepared()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (!active) return
        notifyPlayerCompletion()
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (!active) return
        notifyBufferingUpdate(percent)
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        if (!active) return
//        notifyBuffering(false)
        notifySeekComplete()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!active) return true
        notifyError("what = $what    extra = $extra")
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!active) return true
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
        if (!active) return
        notifyVideoSize(width, height)
    }
}