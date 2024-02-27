package com.mx.mxvideo_demo.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.view.Surface
import com.mx.video.beans.MXPlaySource
import com.mx.video.player.IMXPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class MXIJKPlayer : IMXPlayer(), IMediaPlayer.OnPreparedListener,
    IMediaPlayer.OnCompletionListener, IMediaPlayer.OnBufferingUpdateListener,
    IMediaPlayer.OnSeekCompleteListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnInfoListener,
    IMediaPlayer.OnVideoSizeChangedListener {
    init {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null)
            IjkMediaPlayer.native_profileBegin("libijkplayer.so")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var mediaPlayer: IjkMediaPlayer? = null

    override suspend fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture) {
        val mediaPlayer = IjkMediaPlayer()
        this@MXIJKPlayer.mediaPlayer = mediaPlayer

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.isLooping = false
        mediaPlayer.setOnPreparedListener(this@MXIJKPlayer)
        mediaPlayer.setOnCompletionListener(this@MXIJKPlayer)
        mediaPlayer.setOnBufferingUpdateListener(this@MXIJKPlayer)
        mediaPlayer.setScreenOnWhilePlaying(true)
        mediaPlayer.setOnSeekCompleteListener(this@MXIJKPlayer)
        mediaPlayer.setOnErrorListener(this@MXIJKPlayer)
        mediaPlayer.setOnInfoListener(this@MXIJKPlayer)
        mediaPlayer.setOnVideoSizeChangedListener(this@MXIJKPlayer)

        ////1为硬解 0为软解
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0)
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1)
        mediaPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "mediacodec-handle-resolution-change",
            1
        )
        //使用opensles把文件从java层拷贝到native层
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0)
        //跳帧处理（-1~120）。CPU处理慢时，进行跳帧处理，保证音视频同步
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        //0为一进入就播放,1为进入时不播放
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
        ////域名检测
        mediaPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_FORMAT,
            "http-detect-range-support",
            0
        )
        //设置是否开启环路过滤: 0开启，画面质量高，解码开销大，48关闭，画面质量差点，解码开销小
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
        //最大缓冲大小,单位kb
        mediaPlayer.setOption(
            IjkMediaPlayer.OPT_CATEGORY_PLAYER,
            "max-buffer-size",
            1024 * 1024
        )
        //某些视频在SeekTo的时候，会跳回到拖动前的位置，这是因为视频的关键帧的问题，通俗一点就是FFMPEG不兼容，视频压缩过于厉害，seek只支持关键帧，出现这个情况就是原始的视频文件中i 帧比较少
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)
        //是否重连
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
        //http重定向https
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)
        //设置seekTo能够快速seek到指定位置并播放
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek")
        //播放前的探测Size，默认是1M, 改小一点会出画面更快
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10)
        //1变速变调状态 0变速不变调状态
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1)

        mediaPlayer.setDataSource(context, source.playUri, source.headerMap)
        mediaPlayer.setSurface(Surface(surface))
        withContext(Dispatchers.IO) { mediaPlayer.prepareAsync() }
    }

    override fun enablePreload(): Boolean {
        return true
    }

    override suspend fun start() {
        if (!active) return
        withContext(Dispatchers.IO) { mediaPlayer?.start() }
        notifyStartPlay()
        postBuffering()
    }

    override suspend fun pause() {
        if (!active) return
        withContext(Dispatchers.IO) { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!active) return false
        return mediaPlayer?.isPlaying ?: false
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
        mediaPlayer?.seekTo(time * 1000L)
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

    override fun getPosition(): Float {
        if (!active) return 0f
        return mediaPlayer?.currentPosition?.div(1000f) ?: 0f
    }

    override fun getDuration(): Float {
        if (!active) return 0f
        var duration = mediaPlayer?.duration ?: 0
        if (duration < 0) duration = 0
        return duration / 1000f
    }

    override fun setVolumePercent(leftVolume: Float, rightVolume: Float) {
        if (!active) return
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun setSpeed(speed: Float) {
        if (!active) return
        mediaPlayer?.setSpeed(speed)
    }

    override fun onPrepared(mp: IMediaPlayer?) {
        launch { notifyPrepared() }
    }

    override fun onCompletion(mp: IMediaPlayer?) {
        launch { notifyPlayerCompletion() }
    }

    override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {
        launch { notifyBufferingUpdate(percent) }
    }

    override fun onSeekComplete(mp: IMediaPlayer?) {
        launch { notifySeekComplete() }
    }

    override fun onError(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
        launch { notifyError("what = $what  extra = $extra") }
        return true
    }

    override fun onInfo(mp: IMediaPlayer?, what: Int, extra: Int): Boolean {
        launch {
            when (what) {
                IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    notifyStartPlay()
                }

                IMediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                    notifyBuffering(true)
                }

                IMediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                    notifyBuffering(false)
                }
            }
        }
        return true
    }

    override fun onVideoSizeChanged(mp: IMediaPlayer?, p1: Int, p2: Int, p3: Int, p4: Int) {
        if (!active || mp == null) return
        launch {
            val ratio = p3.toFloat() / p4.toFloat()
            val width = mp.videoWidth
            val height = (mp.videoHeight / ratio).toInt()
            notifyVideoSize(width, height)
        }
    }
}