package com.mx.video.utils

import android.os.Handler

internal class MXTicket {
    private val mHandler = Handler()
    private var isTicketStart = false
    private var timeDiff = 0L
    private var runnable: Runnable? = null
    fun setTicketRun(timeDiff: Long, runnable: Runnable) {
        this.runnable = runnable
        this.timeDiff = timeDiff
    }

    fun start() {
        synchronized(this) {
            isTicketStart = true
            if (runnable != null) {
                mHandler.post(ticketRun)
            }
        }
    }

    fun stop() {
        synchronized(this) {
            isTicketStart = false
            mHandler.removeCallbacksAndMessages(null)
        }
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            synchronized(this) {
                if (!isTicketStart || runnable == null) return
                try {
                    runnable?.run()
                } catch (e: Exception) {
                } finally {
                    mHandler.removeCallbacksAndMessages(null)
                    if (timeDiff > 0) {
                        mHandler.postDelayed(this, timeDiff)
                    }
                }
            }
        }
    }

    fun release() {
        mHandler.removeCallbacksAndMessages(null)
        isTicketStart = false
        runnable = null
    }
}