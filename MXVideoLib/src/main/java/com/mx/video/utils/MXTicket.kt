package com.mx.video.utils

import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread

internal class MXTicket {
    private val mHandler = Handler(Looper.getMainLooper())
    private var diff: Long = 500L
    private var isTicketStart = false
    private var runnable: Runnable? = null

    fun setTicketRun(runnable: Runnable) {
        this.runnable = runnable
    }

    fun setDiffTime(time: Long) {
        diff = time
    }

    fun start() {
        synchronized(this) {
            if (isTicketStart) return
            isTicketStart = true
            thread {
                while (isTicketStart) {
                    runnable?.let {
                        mHandler.post(it)
                    }

                    if (diff > 0) {
                        Thread.sleep(diff)
                    }
                }
            }
        }
    }

    fun stop() {
        synchronized(this) {
            isTicketStart = false
        }
    }

    fun release() {
        MXUtils.log("MXTicket release()")
        isTicketStart = false
        runnable = null
        mHandler.removeCallbacksAndMessages(null)
    }
}