package com.mx.video.player

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

/**
 * 线程切换器
 */
internal class MXThreadHandler {
    private var isStart = false
    private var mHandler: Handler? = null // 主线程Handler
    private var mThreadHandler: Handler? = null // 异步线程Handler
    private var threadHandler: HandlerThread? = null

    fun start() {
        mHandler = Handler(Looper.getMainLooper())
        val thread = HandlerThread("IMXPlayer")
        thread.start()
        mThreadHandler = Handler(thread.looper)
        threadHandler = thread
        isStart = true
    }

    fun stop() {
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
        } finally {
            isStart = false
        }
    }

    fun postInMainThread(run: () -> Unit) {
        if (!isStart) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            run.invoke()
        } else {
            mHandler?.post(run)
        }
    }

    fun postInThread(run: () -> Unit) {
        if (!isStart) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            run.invoke()
        } else {
            mThreadHandler?.post(run)
        }
    }
}