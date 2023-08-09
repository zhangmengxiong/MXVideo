package com.mx.video.utils

import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread

internal class MXTicket {
    private val mHandler = Handler(Looper.getMainLooper())
    private var diff: Long = 200L
    private var isTicketStart = false
    private var runnable: Runnable? = null

    fun setTicketRun(runnable: Runnable) {
        this.runnable = runnable
    }

    fun setDiffTime(time: Long) {
        if (time < 50) return
        diff = time
    }

    fun start() {
        if (isTicketStart) return
        isTicketStart = true

        thread {
            while (isTicketStart) {
                try {
                    runnable?.run()
                    Thread.sleep(diff)
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stop() {
        isTicketStart = false
    }

    fun release() {
        MXUtils.log("MXTicket release()")
        isTicketStart = false
        runnable = null
        mHandler.removeCallbacksAndMessages(null)
    }
}