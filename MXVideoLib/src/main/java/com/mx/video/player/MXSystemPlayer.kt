package com.mx.video.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.view.Surface
import com.mx.video.beans.MXPlaySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MXSystemPlayer : IMXPlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnVideoSizeChangedListener {
    private var mediaPlayer: MediaPlayer? = null
    private var lastSeekTime = 0f

    override suspend fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        lastSeekTime = 0f

        val mediaPlayer = MediaPlayer()
        this@MXSystemPlayer.mediaPlayer = mediaPlayer

        val build = AudioAttributes.Builder()
        build.setUsage(AudioAttributes.USAGE_MEDIA)
        build.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        mediaPlayer.setAudioAttributes(build.build())
        mediaPlayer.isLooping = false
        mediaPlayer.setOnPreparedListener(this@MXSystemPlayer)
        mediaPlayer.setOnCompletionListener(this@MXSystemPlayer)
        mediaPlayer.setOnBufferingUpdateListener(this@MXSystemPlayer)
        mediaPlayer.setScreenOnWhilePlaying(true)
        mediaPlayer.setOnSeekCompleteListener(this@MXSystemPlayer)
        mediaPlayer.setOnErrorListener(this@MXSystemPlayer)
        mediaPlayer.setOnInfoListener(this@MXSystemPlayer)
        mediaPlayer.setOnVideoSizeChangedListener(this@MXSystemPlayer)
        mediaPlayer.setSurface(Surface(surface))
        mediaPlayer.setDataSource(context, source.playUri, source.headerMap)
        withContext(Dispatchers.IO) { mediaPlayer.prepareAsync() }
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
        withContext(Dispatchers.IO) {
            mediaPlayer?.pause()
        }
    }

    override fun isPlaying(): Boolean {
        if (!active) return false
        return (mediaPlayer?.isPlaying == true)
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override suspend fun seekTo(time: Int) = withContext(Dispatchers.IO) {
        val source = source ?: return@withContext
        if (!active || source.isLiveSource) return@withContext

        val duration = getDuration().toInt()
        if (duration != 0 && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            notifyPlayerCompletion()
            return@withContext
        }
        lastSeekTime = time.toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo(time * 1000L, MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(time * 1000)
        }
    }

    override suspend fun release() {
        super.release() // 释放父类资源，必不可少
        this.lastSeekTime = -1f
        val mediaPlayer = mediaPlayer ?: return
        this.mediaPlayer = null
        mediaPlayer.setSurface(null)
        withContext(Dispatchers.IO) {
            mediaPlayer.release()
            delay(200)
        }
    }

    override fun getPosition(): Float {
        if (!active) return 0f
        val position = mediaPlayer?.currentPosition?.div(1000f) ?: 0f
        if (position.toInt() <= 0 && lastSeekTime > 0f) {
            // 修复BUG：seek跳转进度条后，获取进度播放器会概率性返回0的问题！
            return lastSeekTime
        }
        return position
    }

    override fun getDuration(): Float {
        if (!active) return 0f
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0f) duration = 0
        return duration / 1000f
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
        launch { notifyPrepared() }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        launch { notifyPlayerCompletion() }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        launch { notifyBufferingUpdate(percent) }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        launch { notifySeekComplete() }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        launch { notifyError("what = $what  extra = $extra") }
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        launch {
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
        }
        return true
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        launch { notifyVideoSize(width, height) }
    }
}