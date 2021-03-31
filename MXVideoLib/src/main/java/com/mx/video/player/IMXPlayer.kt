package com.mx.video.player

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import com.mx.video.MXPlaySource
import com.mx.video.MXVideo
import java.lang.Exception

abstract class IMXPlayer : TextureView.SurfaceTextureListener {
    var mSurface: Surface? = null
    var mHandler: Handler? = null
    var mThreadHandler: Handler? = null
    private var threadHandler: HandlerThread? = null

    protected fun initHandler() {
        quitHandler()
        mHandler = Handler()
        val threadHandler = HandlerThread("MXSystemPlayer")
        threadHandler.start()
        mThreadHandler = Handler(threadHandler.looper)
        this.threadHandler = threadHandler
    }

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

    fun runInMainThread(run: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            run.invoke()
        } else {
            mHandler?.post(run)
        }
    }

    fun runInThread(run: () -> Unit) {
        mThreadHandler?.post(run)
    }


    private var mMxVideo: MXVideo? = null

    fun getMXVideo() = mMxVideo
    fun setMXVideo(video: MXVideo?) {
        mMxVideo = video
    }

    abstract fun start()

    abstract fun setSource(source: MXPlaySource)

    abstract fun prepare()

    abstract fun pause()

    abstract fun isPlaying(): Boolean

    /**
     * 快进，单位：秒
     */
    abstract fun seekTo(time: Int)

    /**
     * 释放资源
     */
    abstract fun release()

    /**
     * 获取当前播放时间，单位：秒
     */
    abstract fun getCurrentPosition(): Int

    /**
     * 返回播放总时长，单位：秒
     */
    abstract fun getDuration(): Int

    abstract fun setVolume(leftVolume: Float, rightVolume: Float)

    /**
     * 设置播放速度
     */
    abstract fun setSpeed(speed: Float)
}