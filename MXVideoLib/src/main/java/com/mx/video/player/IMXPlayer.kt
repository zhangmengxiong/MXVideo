package com.mx.video.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import com.mx.video.base.IMXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.views.MXTextureView
import java.util.concurrent.atomic.AtomicBoolean

/**
 * API调用流程：
 * 1：setSource()
 *      设置播放源信息
 *
 * 2：setMXVideo()
 *      设置播放器相关
 *
 * 3：prepare()
 *      在TextureView准备好了的时候，回调prepare
 *
 * 4：notifyPrepared()
 *      播放器回调状态 -> 预备完成
 *
 * 5：notifyStartPlay()
 *      播放器回调状态 -> 开始播放
 *
 * 6：notifyBuffering()
 *      播放器回调状态 -> 缓冲开始/结束
 *
 * 7：notifyPlayerCompletion() / notifyError()
 *      播放器回调状态 -> 播放完成/播放错误
 *
 * 8：release()
 *      释放资源
 */
abstract class IMXPlayer : TextureView.SurfaceTextureListener {
    private val isActive = AtomicBoolean(false)
    private val mxHandler = MXThreadHandler()
    private var isBuffering = false
    private var isPrepared = false
    private var isStartPlay = false

    private var mContext: Context? = null
    private var video: IMXVideo? = null
    private var mTextureView: MXTextureView? = null
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mPlaySource: MXPlaySource? = null
    protected val source: MXPlaySource?
        get() = mPlaySource

    /**
     * 播放器是否可用
     */
    val active: Boolean
        get() = isActive.get()

    /**
     * 在主线程中运行
     * @param run 运行回调
     */
    fun postInMainThread(run: () -> Unit) {
        mxHandler.postInMainThread {
            if (active) {
                run.invoke()
            }
        }
    }

    /**
     * 在Thread中运行
     * @param run 运行回调
     */
    fun postInThread(run: () -> Unit) {
        mxHandler.postInThread {
            if (active) {
                run.invoke()
            }
        }
    }

    internal fun startPlay(
        context: Context,
        video: IMXVideo,
        source: MXPlaySource,
        textureView: MXTextureView
    ) {
        this.mContext = context
        this.video = video
        this.mTextureView = textureView
        this.mPlaySource = source

        this.isBuffering = false
        this.isPrepared = false
        this.isStartPlay = false
        this.hasPrepareCall = false

        mxHandler.start()
        isActive.set(true)
        mTextureView?.surfaceTextureListener = this
        requestPrepare()
    }

    private var hasPrepareCall = false
    private fun requestPrepare() {
        if (!active) return
        if (hasPrepareCall) return
        val context = mContext ?: return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
        video?.onPlayerInfo(" --> prepare <--")
        prepare(context, source, surface)
        hasPrepareCall = true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!active) return
        val texture = mSurfaceTexture
        if (texture == null) {
            mSurfaceTexture = surface
            requestPrepare()
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

    /**
     * 开始加载视频
     */
    abstract fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture)

    /**
     * 当播放器prepare后调用，开始播放
     */
    abstract fun start()

    /**
     * 暂停
     */
    abstract fun pause()

    /**
     * 是否正在播放中
     */
    abstract fun isPlaying(): Boolean

    /**
     * 快进，单位：秒
     */
    abstract fun seekTo(time: Int)

    /**
     * 释放资源
     */
    open fun release() {
        isActive.set(false)
        mxHandler.stop()
        video?.onPlayerInfo(" --> release <--")

        isBuffering = false
        isPrepared = false
        isStartPlay = false
        hasPrepareCall = false

        mContext = null
        video = null
        mTextureView = null
        mSurfaceTexture = null
        Runtime.getRuntime().gc()
    }

    /**
     * 获取当前播放时间，单位：秒
     */
    abstract fun getPosition(): Int

    /**
     * 返回播放总时长，单位：秒
     */
    abstract fun getDuration(): Int

    /**
     * 设置播放器音量百分比
     * 静音 = 0f ,   默认 = 1f
     * 取值范围：0.0 -> 1.0
     */
    abstract fun setVolumePercent(leftVolume: Float, rightVolume: Float)

    /**
     * 设置播放速度
     */
    abstract fun setSpeed(speed: Float)

    /**
     * 是否支持预加载
     */
    open fun enablePreload() = false

    /**
     * 播放错误
     */
    protected fun notifyError(message: String?) {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            release()
            video.onPlayerError(message)
        }
    }

    /**
     * 视频宽高
     */
    protected fun notifyVideoSize(width: Int, height: Int) {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            video.onPlayerVideoSizeChanged(width, height)
        }
    }

    /**
     * seek完成回调
     */
    protected fun notifySeekComplete() {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            video.onPlayerSeekComplete()
        }
    }

    /**
     * 播放完成
     */
    protected fun notifyPlayerCompletion() {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            release()
            video.onPlayerCompletion()
        }
    }

    /**
     * 缓冲进度更新
     */
    protected fun notifyBufferingUpdate(percent: Int) {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            video.onPlayerBufferProgress(percent)
        }
    }

    /**
     * 缓冲状态更新
     * @param start true=正在缓冲，false=缓冲完成
     */
    protected fun notifyBuffering(start: Boolean) {
        if (!active) return
        if (!isPrepared || !isStartPlay) return
        if (isBuffering == start) return
        val video = video ?: return
        isBuffering = start
        mxHandler.postInMainThread {
            video.onPlayerBuffering(start)
        }
    }

    /**
     * 重新设置加载中状态
     */
    protected fun postBuffering() {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            video.onPlayerBuffering(isBuffering)
        }
    }

    /**
     * 播放器准备完成，可以调用#start()方法播放
     */
    protected fun notifyPrepared() {
        if (!active) return
        if (isPrepared) return
        val video = video ?: return

        isPrepared = true
        mxHandler.postInMainThread {
            video.onPlayerPrepared()
        }
    }

    /**
     * 播放正式开始！
     */
    protected fun notifyStartPlay() {
        if (!active) return
        if (!isPrepared) return
        if (isStartPlay) return
        val video = video ?: return
        isStartPlay = true
        mxHandler.postInMainThread {
            video.onPlayerStartPlay()
        }
    }

    /**
     * 播放信息输出
     */
    protected fun onPlayerInfo(message: String?) {
        if (!active) return
        val video = video ?: return
        mxHandler.postInMainThread {
            video.onPlayerInfo(message)
        }
    }
}