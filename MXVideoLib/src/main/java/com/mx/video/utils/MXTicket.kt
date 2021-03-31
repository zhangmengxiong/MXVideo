package com.mx.video.utils

import android.os.Handler
import java.lang.Exception

class MXTicket {
    private val mHandler = Handler()
    private var isTicketStart = false
    private var timeDiff = 300L
    private var runnable: Runnable? = null
    fun setTicketRun(timeDiff: Long = 300, runnable: Runnable) {
        this.runnable = runnable
        this.timeDiff = timeDiff
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
                mHandler.postDelayed(this, timeDiff)
            }
        }
    }
}