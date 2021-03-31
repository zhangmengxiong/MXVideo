package com.mx.video.player

import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.view.Surface
import com.mx.video.MXPlaySource
import com.mx.video.MXVideo

class MXSystemPlayer : IMXPlayer(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
    MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnVideoSizeChangedListener {
    var mediaPlayer: MediaPlayer? = null
    var mPlaySource: MXPlaySource? = null
    override fun start() {
        runInThread { mediaPlayer?.start() }
    }

    override fun setSource(source: MXPlaySource) {
        mPlaySource = source
    }

    override fun prepare() {
        val source = mPlaySource ?: return
        releaseNow()
        initHandler()
        runInThread {
            val mediaPlayer = MediaPlayer()
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
            try {
                val clazz = MediaPlayer::class.java
                //如果不用反射，没有url和header参数的setDataSource函数
                val method = clazz.getDeclaredMethod(
                    "setDataSource",
                    String::class.java,
                    Map::class.java
                )
                method.isAccessible = true
                method.invoke(
                    mediaPlayer,
                    source.playUrl,
                    source.headerMap
                )
            } catch (e: Exception) {
                mediaPlayer.setDataSource(
                    MXVideo.getAppContext(),
                    Uri.parse(source.playUrl),
                    source.headerMap
                )
            }
            mediaPlayer.prepareAsync()
            mediaPlayer.setSurface(Surface(mSurface))
            this.mediaPlayer = mediaPlayer
        }
    }

    override fun pause() {
        runInThread { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun seekTo(time: Int) {
        runInThread { mediaPlayer?.seekTo(time * 1000) }
    }

    override fun release() {
        super.release() // 释放父类资源，必不可少

        val mediaPlayer = mediaPlayer
        this.mediaPlayer = null
        mSurface = null

        runInThread {
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
        return mediaPlayer?.duration?.div(1000) ?: 0
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun setSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pp = mediaPlayer?.playbackParams ?: return
            pp.speed = speed
            mediaPlayer?.playbackParams = pp
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        if (mSurface == null) {
            mSurface = surface
            prepare()
        } else {
            mTextureView?.surfaceTexture = mSurface
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
        runInMainThread { getMXVideo()?.onPrepared() }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        runInMainThread { getMXVideo()?.onCompletion() }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        runInMainThread { getMXVideo()?.setBufferProgress(percent) }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        runInMainThread { getMXVideo()?.onSeekComplete() }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        runInMainThread {
            getMXVideo()?.onError()
            release()
        }
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                runInMainThread { getMXVideo()?.onRenderFirstFrame() }
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                runInMainThread { getMXVideo()?.onBuffering(true) }
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                runInMainThread { getMXVideo()?.onBuffering(false) }
            }
        }
        return true
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        runInMainThread { getMXVideo()?.onVideoSizeChanged(width, height) }
    }
}