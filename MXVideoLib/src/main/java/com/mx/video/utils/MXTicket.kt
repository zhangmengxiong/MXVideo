package com.mx.video.utils

import android.os.Handler
import android.os.Looper

internal class MXTicket {
    private val mHandler = Handler(Looper.getMainLooper())
    private var isTicketStart = false
    private var runnable: Runnable? = null
    fun setTicketRun(runnable: Runnable) {
        this.runnable = runnable
    }

    fun start() {
        isTicketStart = true
        mHandler.removeCallbacksAndMessages(null)
        mHandler.post(ticketRun)
    }

    fun stop() {
        isTicketStart = false
        mHandler.removeCallbacksAndMessages(null)
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            val runnable = runnable
            if (!isTicketStart || runnable == null) return
            try {
                runnable.run()
            } catch (_: Exception) {
            } finally {
                mHandler.postDelayed(this, 500)
            }
        }
    }

    fun release() {
        isTicketStart = false
        runnable = null
        mHandler.removeCallbacksAndMessages(null)
    }
}