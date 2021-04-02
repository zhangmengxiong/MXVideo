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
        if (!isActive()) return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
        releaseNow()
        initHandler()
        runInThread {
            if (!isActive()) return@runInThread

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

            mediaPlayer.setDataSource(
                MXVideo.getAppContext(),
                Uri.parse(source.playUrl),
                source.headerMap
            )

            mediaPlayer.prepareAsync()
            mediaPlayer.setSurface(Surface(surface))
            this.mediaPlayer = mediaPlayer
        }
    }

    override fun pause() {
        if (!isActive()) return
        runInThread { mediaPlayer?.pause() }
    }

    override fun isPlaying(): Boolean {
        if (!isActive()) return false
        return mediaPlayer?.isPlaying ?: false
    }

    override fun seekTo(time: Int) {
        if (!isActive()) return

        runInThread {
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
        runInMainThread { getMXVideo()?.onPrepared() }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (!isActive()) return
        runInMainThread { getMXVideo()?.onCompletion() }
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
        if (!isActive()) return
        runInMainThread { getMXVideo()?.setBufferProgress(percent) }
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        if (!isActive()) return
        runInMainThread { getMXVideo()?.onSeekComplete() }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        runInMainThread {
            getMXVideo()?.onError("what = $what    extra = $extra")
            release()
        }
        return true
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (!isActive()) return true
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
//                runInMainThread { getMXVideo()?.onRenderFirstFrame() }
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
        if (!isActive()) return
        runInMainThread { getMXVideo()?.onVideoSizeChanged(width, height) }
    }
}