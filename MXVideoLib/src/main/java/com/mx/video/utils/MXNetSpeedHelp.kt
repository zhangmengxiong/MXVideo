package com.mx.video.utils

import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * 网速检测
 */
internal class MXNetSpeedHelp {
    private val myUid = android.os.Process.myUid()
    private val mHandler = Handler(Looper.getMainLooper())
    private var onUpdateCall: ((String?) -> Unit)? = null
    private var isStart = false

    private var preRxBytes = 0L
    private var preRxTime = 0L

    fun start() {
        isStart = true
        preRxBytes = 0L
        preRxTime = 0L
        mHandler.removeCallbacksAndMessages(null)
        onUpdateCall?.invoke(null)
        mHandler.post(ticketRun)
    }

    fun stop() {
        isStart = false
        preRxBytes = 0L
        preRxTime = 0L
        mHandler.removeCallbacksAndMessages(null)
        onUpdateCall?.invoke(null)
    }

    fun release() {
        isStart = false
        onUpdateCall = null
        mHandler.removeCallbacksAndMessages(null)
    }

    private val ticketRun = object : Runnable {
        override fun run() {
            val onUpdateCall = onUpdateCall
            if (!isStart || onUpdateCall == null) return
            try {
                val rxBytes = TrafficStats.getUidRxBytes(myUid)
                val time = System.currentTimeMillis()
                if (preRxBytes > 0 && preRxTime > 0) {
                    val diffBytes = abs(rxBytes - preRxBytes)
                    val diffTime = abs(time - preRxTime) / 1000f
                    val speed = if (diffTime > 0 && diffBytes > 0) {
                        diffBytes / diffTime
                    } else 0f
                    onUpdateCall.invoke(MXUtils.byteToShow(speed.roundToLong()))
                }
                preRxBytes = rxBytes
                preRxTime = time
            } catch (e: Exception) {
            } finally {
                mHandler.postDelayed(this, 500)
            }
        }
    }

    fun setOnSpeedUpdate(call: ((String?) -> Unit)) {
        onUpdateCall = call
    }
}