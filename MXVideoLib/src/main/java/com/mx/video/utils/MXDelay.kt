package com.mx.video.utils

import android.os.Handler

class MXDelay {
    private val mHandler = Handler()
    private var isTicketStart = false
    private var timeDelay = 5000L
    private var runnable: Runnable? = null
    fun setDelayRun(timeDelay: Long = 300, runnable: Runnable) {
        this.runnable = runnable
        this.timeDelay = timeDelay
    }

    fun start() {
        isTicketStart = true
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed(ticketRun, timeDelay)
    }

    fun stop() {
        isTicketStart = false
        mHandler.removeCallbacksAndMessages(null)
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            if (!isTicketStart || runnable == null) return
            runnable?.run()
        }
    }

    fun release() {
        mHandler.removeCallbacksAndMessages(null)
        isTicketStart = false
        runnable = null
    }
}