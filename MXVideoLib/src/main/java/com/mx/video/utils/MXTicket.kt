package com.mx.video.utils

import com.mx.video.beans.ITicketCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

internal class MXTicket {
    private var scope: CoroutineScope? = null
    private var isStartTicket = false
    private var diff: Long = 200L
    private var ticketRun: ITicketCallback? = null

    fun setTicketRun(runnable: ITicketCallback) {
        this.ticketRun = runnable
    }

    fun setDiffTime(time: Long) {
        diff = max(time, 100)
    }

    fun start() {
        isStartTicket = true
        synchronized(this) {
            scope?.cancel()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            scope?.launch(Dispatchers.IO) {
                while (isActive && isStartTicket) {
                    launch(Dispatchers.Main) {
                        ticketRun?.ticket()
                    }
                    Thread.sleep(diff)
                }
            }
        }
    }

    fun stop() {
        synchronized(this) {
            isStartTicket = false
            scope?.cancel()
            scope = null
        }
    }

    fun release() {
        MXUtils.log("MXTicket release()")
        isStartTicket = false
        ticketRun = null
        scope?.cancel()
        scope = null
    }
}