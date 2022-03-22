package com.mx.video.utils

import android.os.Handler

internal class MXDelay {
    private val mHandler = Handler()
    private var isTicketStart = false
    private var timeDelay = 2000L
    private var runnable: Runnable? = null
    fun setDelayRun(timeDelay: Long, runnable: Runnable) {
        this.runnable = runnable
        this.timeDelay = timeDelay
    }

    fun start() {
        synchronized(this) {
            isTicketStart = true
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed(ticketRun, timeDelay)
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
                runnable?.run()
            }
        }
    }

    fun release() {
        isTicketStart = false
        runnable = null
        mHandler.removeCallbacksAndMessages(null)
    }
}