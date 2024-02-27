package com.mx.mxvideo_demo.player.vlc

import android.content.Context
import android.graphics.SurfaceTexture
import com.mx.video.beans.MXPlaySource
import com.mx.video.player.IMXPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class MXVLCPlayer : IMXPlayer() {
    private var mLibVLC: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var lastSeekTime = 0f

    override suspend fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) =
        withContext(Dispatchers.IO) {
            lastSeekTime = 0f
            val options: ArrayList<String> = VlcOptions.getLibOptions(context)
            val libVLC = LibVLC(context, options)
            val mediaPlayer = MediaPlayer(libVLC)
            mediaPlayer.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.EndReached -> launch {
                        notifyPlayerCompletion()
                    }

                    MediaPlayer.Event.Playing -> launch {
                        notifyPrepared()
                        notifyBuffering(false)
                    }

                    MediaPlayer.Event.Opening -> launch {
                        notifyBuffering(true)
                    }

                    MediaPlayer.Event.EncounteredError -> launch {
                        notifyError("what = $event ")
                    }

                    MediaPlayer.Event.Buffering -> launch {
                        val buffering = event.buffering.toInt()
                        if (buffering >= 100) {
                            notifyBuffering(false)
                        } else {
                            notifyBuffering(true)
                            notifyBufferingUpdate(buffering)
                        }
                    }

                    else -> Unit
                }
            }

            this@MXVLCPlayer.mLibVLC = libVLC
            this@MXVLCPlayer.mMediaPlayer = mediaPlayer

            val media = Media(libVLC, source.playUri)
            media.parse()
            mediaPlayer.media = media
            media.release()
            mediaPlayer.videoScale = MediaPlayer.ScaleType.SURFACE_FILL
            mediaPlayer.play()

            mediaPlayer.vlcVout.detachViews()
            mediaPlayer.vlcVout.setVideoSurface(surface)
            mediaPlayer.vlcVout.attachViews { _, width, height, _, _, _, _ ->
                if (width <= 0 || height <= 0) return@attachViews
                launch { notifyVideoSize(width, height) }
            }
            mediaPlayer.scale = 0f
        }

    override fun enablePreload(): Boolean {
        return false
    }

    override suspend fun start() {
        if (!active) return
        withContext(Dispatchers.IO) { mMediaPlayer?.play() }
        notifyStartPlay()
        postBuffering()
    }

    override suspend fun pause() {
        if (!active) return
        withContext(Dispatchers.IO) { mMediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!active) return false
        return (mMediaPlayer?.isPlaying == true)
    }

    // 这里不需要处理未播放状态的快进快退，MXVideo会判断。
    override suspend fun seekTo(time: Int) = withContext(Dispatchers.IO) {
        val source = source ?: return@withContext
        if (!active || source.isLiveSource) return@withContext
        val mediaPlayer = mMediaPlayer ?: return@withContext
        if (!mediaPlayer.isSeekable) return@withContext

        val duration = getDuration().toInt()
        if (duration != 0 && time >= duration) {
            // 如果直接跳转到结束位置，则直接complete
            notifyPlayerCompletion()
            return@withContext
        }
        lastSeekTime = time.toFloat()
        mediaPlayer.setTime(time * 1000L, true)
    }

    override suspend fun release() {
        super.release() // 释放父类资源，必不可少
        this.lastSeekTime = -1f
        val mediaPlayer = mMediaPlayer ?: return
        this.mMediaPlayer = null
        mediaPlayer.stop();
        mediaPlayer.setEventListener(null);
        mediaPlayer.vlcVout.detachViews();
        withContext(Dispatchers.IO) {
            mediaPlayer.release()
            mLibVLC?.release();
            mLibVLC = null;
            delay(200)
        }
    }

    override fun getPosition(): Float {
        if (!active) return 0f
        val mediaPlayer = mMediaPlayer ?: return 0f
        val position = mediaPlayer.time.div(1000f)
        if (position.toInt() <= 0 && lastSeekTime > 0f) {
            // 修复BUG：seek跳转进度条后，获取进度播放器会概率性返回0的问题！
            return lastSeekTime
        }
        return position
    }

    override fun getDuration(): Float {
        if (!active) return 0f
        val mediaPlayer = mMediaPlayer ?: return 0f
        var duration = mediaPlayer.length
        if (duration < 0f) duration = 0
        return duration / 1000f
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        val mediaPlayer = mMediaPlayer ?: return
        mediaPlayer.setVolume((leftVolume * 100).toInt())
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        val mediaPlayer = mMediaPlayer ?: return
        mediaPlayer.rate = speed
    }

}