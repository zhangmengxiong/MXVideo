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
    private var diffTime: Long = 1000L
    private var playSpeed = 1f
    private var ticketRun: ITicketCallback? = null
    private var diffMiles = 200L

    fun setTicketRun(runnable: ITicketCallback) {
        this.ticketRun = runnable
    }

    fun setDiffTime(time: Long) {
        diffTime = time
        diffMiles = max((diffTime / (2.0 * playSpeed)).toLong(), 100L)
    }

    fun setPlaySpeed(speed: Float) {
        playSpeed = if (speed <= 0) 1f else speed
        diffMiles = max((diffTime / (2.0 * playSpeed)).toLong(), 100L)
    }

    fun start() {
        isStartTicket = true
        synchronized(this) {
            scope?.cancel()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            scope?.launch(Dispatchers.IO) {
                while (isActive && isStartTicket) {
                    launch(Dispatchers.Main) {
                        if (isStartTicket) ticketRun?.ticket()
                    }
                    Thread.sleep(diffMiles)
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