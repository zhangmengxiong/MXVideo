package com.mx.video.utils

import android.os.Handler

internal class MXTicket {
    private val mHandler = Handler()
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
            if (!isTicketStart) return
            try {
                runnable?.run()
            } catch (e: Exception) {
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