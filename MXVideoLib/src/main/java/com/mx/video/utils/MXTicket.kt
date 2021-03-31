package com.mx.video.utils

import android.os.Handler
import java.lang.Exception

class MXTicket {
    private val mHandler = Handler()
    private var isTicketStart = false
    private var ticketTimeDiff = 300L
    private var runnable: Runnable? = null
    fun setTicketRun(ticketTimeDiff: Long = 300, runnable: Runnable) {
        this.runnable = runnable
        this.ticketTimeDiff = ticketTimeDiff
        if (isTicketStart) {
            mHandler.post(ticketRun)
        }
    }

    fun start() {
        isTicketStart = true
        if (runnable != null) {
            mHandler.post(ticketRun)
        }
    }

    fun stop() {
        isTicketStart = false
        mHandler.removeCallbacksAndMessages(null)
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            if (!isTicketStart || runnable == null) return
            try {
                runnable?.run()
            } catch (e: Exception) {
            } finally {
                mHandler.postDelayed(this, ticketTimeDiff)
            }
        }
    }
}