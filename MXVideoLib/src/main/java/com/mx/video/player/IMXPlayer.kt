package com.mx.video.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
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
    private val isActive = AtomicBoolean(true)
    private var isBuffering = false
    private var isPrepared = false
    private var isStartPlay = false

    private var mHandler: Handler? = null // 主线程Handler
    private var mThreadHandler: Handler? = null // 异步线程Handler
    private var threadHandler: HandlerThread? = null

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
     * 初始化主线程Handler和Thread进程
     */
    private fun initHandler() {
        mHandler = Handler(Looper.getMainLooper())
        val thread = HandlerThread("IMXPlayer")
        thread.start()
        mThreadHandler = Handler(thread.looper)
        threadHandler = thread
    }

    /**
     * 取消所有运行方法，包含主线程和Thread线程
     */
    private fun quitHandler() {
        try {
            val th = threadHandler
            val mh = mHandler
            val mth = mThreadHandler
            threadHandler = null
            mHandler = null
            mThreadHandler = null

            th?.quit()
            mh?.removeCallbacksAndMessages(null)
            mth?.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
        }
    }

    /**
     * 在主线程中运行
     * @param run 运行回调
     */
    fun postInMainThread(run: () -> Unit) {
        if (!active) return
        if (mHandler == null) {
            initHandler()
        }
        val handler = mHandler ?: return

        // 包装一下，抓异常
        val catchRun = {
            try {
                run.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            catchRun.invoke()
        } else {
            handler.post(catchRun)
        }
    }

    /**
     * 在Thread中运行
     * @param run 运行回调
     */
    fun postInThread(run: () -> Unit) {
        if (!active) return
        if (mThreadHandler == null) {
            initHandler()
        }
        mThreadHandler?.post {
            // 包装一下，抓异常
            try {
                run.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setMXVideo(context: Context, video: IMXVideo, textureView: MXTextureView) {
        this.mContext = context
        this.video = video
        this.mTextureView = textureView
        isActive.set(true)

        this.isBuffering = false
        this.isPrepared = false
        this.isStartPlay = false
        this.hasPrepareCall = false
    }

    internal fun setSource(source: MXPlaySource) {
        mPlaySource = source
    }

    private var hasPrepareCall = false
    fun requestPrepare() {
        if (!active) return
        if (hasPrepareCall) return
        val context = mContext ?: return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
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
        quitHandler()

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
     * 设置播放器音量
     * 取值范围：0.0 -> 1.0
     */
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

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
        postInMainThread {
            video.onPlayerError(message)
        }
        release()
    }

    /**
     * 视频宽高
     */
    protected fun notifyVideoSize(width: Int, height: Int) {
        if (!active) return
        val video = video ?: return
        postInMainThread {
            video.onPlayerVideoSizeChanged(width, height)
        }
    }

    /**
     * seek完成回调
     */
    protected fun notifySeekComplete() {
        if (!active) return
        val video = video ?: return
        postInMainThread {
            video.onPlayerSeekComplete()
        }
    }

    /**
     * 播放完成
     */
    protected fun notifyPlayerCompletion() {
        if (!active) return
        val video = video ?: return
        postInMainThread {
            video.onPlayerCompletion()
        }
        release()
    }

    /**
     * 缓冲进度更新
     */
    protected fun notifyBufferingUpdate(percent: Int) {
        if (!active) return
        val video = video ?: return
        postInMainThread {
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
        postInMainThread {
            video.onPlayerBuffering(start)
        }
    }

    /**
     * 重新设置加载中状态
     */
    protected fun postBuffering() {
        if (!active) return
        val video = video ?: return
        postInMainThread {
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
        postInMainThread {
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
        postInMainThread {
            video.onPlayerStartPlay()
        }
    }

    /**
     * 播放信息输出
     */
    protected fun onPlayerInfo(message: String?) {
        if (!active) return
        val video = video ?: return
        postInMainThread {
            video.onPlayerInfo(message)
        }
    }
}