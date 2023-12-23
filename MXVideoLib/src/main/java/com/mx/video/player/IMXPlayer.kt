package com.mx.video.player

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.TextureView
import com.mx.video.base.IMXPlayerCallback
import com.mx.video.beans.MXPlaySource
import com.mx.video.utils.MXUtils
import com.mx.video.views.MXTextureView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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
    private var scope: CoroutineScope? = null
    private var isBuffering = false
    private var isPrepared = false
    private var isStartPlay = false

    private var mContext: Context? = null
    private var playerCallback: IMXPlayerCallback? = null
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

    fun launch(
        context: CoroutineContext = Dispatchers.Main,
        block: (suspend CoroutineScope.() -> Unit)
    ) {
        if (!active) return
        scope?.launch(context = context, block = block)
    }

    internal fun startPlay(
        context: Context,
        callback: IMXPlayerCallback,
        source: MXPlaySource,
        textureView: MXTextureView
    ) {
        this.mContext = context
        this.playerCallback = callback
        this.mTextureView = textureView
        this.mPlaySource = source

        this.isBuffering = false
        this.isPrepared = false
        this.isStartPlay = false
        this.hasPrepareCall = false

        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        isActive.set(true)
        mTextureView?.surfaceTextureListener = this
        scope?.launch { requestPrepare() }
    }

    private var hasPrepareCall = false
    private suspend fun requestPrepare() {
        if (!active) return
        if (hasPrepareCall) return
        val context = mContext ?: return
        val source = mPlaySource ?: return
        val surface = mSurfaceTexture ?: return
        playerCallback?.onPlayerInfo(" --> prepare <--")
        prepare(context, source, surface)
        hasPrepareCall = true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (!active) return
        val texture = mSurfaceTexture
        if (texture == null) {
            mSurfaceTexture = surface
            scope?.launch {
                requestPrepare()
                playerCallback?.onPlayerInfo(" --> onSurfaceTextureAvailable / null <--")
            }
        } else {
            mTextureView?.setSurfaceTexture(texture)
            playerCallback?.onPlayerInfo(" --> onSurfaceTextureAvailable / NotNull <--")
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
    abstract suspend fun prepare(context: Context, source: MXPlaySource, surface: SurfaceTexture)

    /**
     * 当播放器prepare后调用，开始播放
     */
    abstract suspend fun start()

    /**
     * 暂停
     */
    abstract suspend fun pause()

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
    open suspend fun release() {
        isActive.set(false)
        scope = null
        playerCallback?.onPlayerInfo(" --> release <--")

        isBuffering = false
        isPrepared = false
        isStartPlay = false
        hasPrepareCall = false

        mContext = null
        playerCallback = null

        mTextureView?.release()
        mTextureView = null
        mSurfaceTexture = null
        Runtime.getRuntime().gc()
    }

    /**
     * 获取当前播放时间，单位：秒
     */
    abstract fun getPosition(): Float

    /**
     * 返回播放总时长，单位：秒
     */
    abstract fun getDuration(): Float

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
    protected suspend fun notifyError(message: String) = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        val source = mPlaySource ?: return@withContext
        release()
        callback.onPlayerError(source, message)
    }

    /**
     * 视频宽高
     */
    protected suspend fun notifyVideoSize(width: Int, height: Int) = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        callback.onPlayerVideoSizeChanged(width, height)
    }

    /**
     * seek完成回调
     */
    protected suspend fun notifySeekComplete() = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        callback.onPlayerSeekComplete()
    }

    /**
     * 播放完成
     */
    protected suspend fun notifyPlayerCompletion() = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        release()
        callback.onPlayerCompletion()
    }

    /**
     * 缓冲进度更新
     */
    protected suspend fun notifyBufferingUpdate(percent: Int) = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        callback.onPlayerBufferProgress(percent)
    }

    /**
     * 缓冲状态更新
     * @param start true=正在缓冲，false=缓冲完成
     */
    protected suspend fun notifyBuffering(start: Boolean) = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        if (!isPrepared || !isStartPlay) return@withContext
        if (isBuffering == start) return@withContext
        val callback = playerCallback ?: return@withContext
        isBuffering = start
        callback.onPlayerBuffering(start)
    }

    /**
     * 重新设置加载中状态
     */
    protected suspend fun postBuffering() = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        callback.onPlayerBuffering(isBuffering)
    }

    /**
     * 播放器准备完成，可以调用#start()方法播放
     */
    protected suspend fun notifyPrepared() = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        if (isPrepared) return@withContext
        val callback = playerCallback ?: return@withContext

        isPrepared = true
        callback.onPlayerPrepared()
    }

    /**
     * 播放正式开始！
     */
    protected suspend fun notifyStartPlay() = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        if (!isPrepared) return@withContext
        if (isStartPlay) return@withContext
        val callback = playerCallback ?: return@withContext
        isStartPlay = true
        callback.onPlayerStartPlay()
    }

    /**
     * 播放信息输出
     */
    protected suspend fun onPlayerInfo(message: String?) = withContext(Dispatchers.Main) {
        if (!active) return@withContext
        val callback = playerCallback ?: return@withContext
        callback.onPlayerInfo(message)
    }
}