package com.mx.video.utils

import android.os.Handler
import android.os.Looper

internal class MXDelay {
    private val mHandler = Handler(Looper.getMainLooper())
    private var isTicketStart = false
    private var timeDelay = 4000L
    private var runnable: Runnable? = null
    fun setDelayRun(timeDelay: Long, runnable: Runnable) {
        this.runnable = runnable
        this.timeDelay = timeDelay
    }

    fun delay() {
        if (!isTicketStart) return
        synchronized(this) {
            mHandler.removeCallbacksAndMessages(null)
            mHandler.postDelayed(ticketRun, timeDelay)
        }
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
                isTicketStart = false
            }
        }
    }

    fun release() {
        isTicketStart = false
        runnable = null
        mHandler.removeCallbacksAndMessages(null)
    }
}