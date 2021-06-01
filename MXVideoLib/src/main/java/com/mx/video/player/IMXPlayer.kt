package com.mx.video.player

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.TextureView
import com.mx.video.MXVideo
import com.mx.video.beans.MXPlaySource
import com.mx.video.views.MXTextureView
import java.util.concurrent.atomic.AtomicBoolean

abstract class IMXPlayer : TextureView.SurfaceTextureListener {
    protected var mSurfaceTexture: SurfaceTexture? = null
    protected var mTextureView: MXTextureView? = null

    private var mMxVideo: MXVideo? = null
    private val isActive = AtomicBoolean(false)

    /**
     * 播放器是否可用
     */
    fun isActive() = isActive.get()

    var mHandler: Handler? = null
    var mThreadHandler: Handler? = null
    private var threadHandler: HandlerThread? = null

    /**
     * 初始化主线程Handler和Thread进程
     */
    protected fun initHandler() {
        quitHandler()
        mHandler = Handler()
        val threadHandler = HandlerThread("IMXPlayer")
        threadHandler.start()
        mThreadHandler = Handler(threadHandler.looper)
        this.threadHandler = threadHandler
    }

    /**
     * 取消所有运行方法，包含主线程和Thread线程
     */
    protected fun quitHandler() {
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
        if (!isActive.get()) return
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
        mThreadHandler?.post {
            // 包装一下，抓异常
            try {
                run.invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取MXVideo主体
     */
    fun getMXVideo(): MXVideo? {
        return if (isActive.get()) mMxVideo else null
    }

    fun setMXVideo(video: MXVideo, textureView: MXTextureView) {
        mMxVideo = video
        mTextureView = textureView
        isActive.set(true)
    }

    /**
     * 当播放器prepare后调用，开始播放
     */
    abstract fun start()

    /**
     * 设置播放源
     */
    abstract fun setSource(source: MXPlaySource)

    /**
     * 开始加载视频
     */
    abstract fun prepare()

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
        mMxVideo = null
        mTextureView = null
        Runtime.getRuntime().gc()
    }

    /**
     * 获取当前播放时间，单位：秒
     */
    abstract fun getCurrentPosition(): Int

    /**
     * 返回播放总时长，单位：秒
     */
    abstract fun getDuration(): Int

    /**
     * 设置播放器音量
     */
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 设置播放速度
     */
    abstract fun setSpeed(speed: Float)
}