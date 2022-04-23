package com.mx.video.utils

import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import java.lang.Exception
import kotlin.math.abs

/**
 * 网速检测
 */
internal class MXSpeedHelp {
    private var onUpdateCall: ((String?) -> Unit)? = null
    private val mHandler = Handler(Looper.getMainLooper())
    private var isStart = false

    private var speedNow = -1L
    private var preSize = 0L

    fun start() {
        isStart = true
        mHandler.removeCallbacksAndMessages(null)
        mHandler.post(ticketRun)
    }

    fun release() {
        isStart = false
        onUpdateCall = null
        mHandler.removeCallbacksAndMessages(null)
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            if (!isStart) return
            try {
                val rxBytes = TrafficStats.getUidRxBytes(android.os.Process.myUid())
                if (preSize > 0) {
                    speedNow = abs(rxBytes - preSize)
                }
                preSize = rxBytes
                onUpdateCall?.invoke(getSpeed())
            } catch (e: Exception) {
            } finally {
                mHandler.postDelayed(this, 1000)
            }
        }
    }

    private fun getSpeed(): String? {
        return when {
            speedNow <= 0 -> null
            speedNow < 1024 -> {
                "$speedNow Byte/s"
            }
            speedNow < 1024 * 1024 -> {
                "${speedNow / 1024} Kb/s"
            }
            else -> "${speedNow / (1024 * 1024)} Mb/s"
        }
    }

    fun setOnSpeedUpdate(call: ((String?) -> Unit)) {
        onUpdateCall = call
    }
}