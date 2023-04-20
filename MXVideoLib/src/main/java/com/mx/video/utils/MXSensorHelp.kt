package com.mx.video.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import com.mx.video.beans.MXOrientation
import com.mx.video.listener.MXSensorListener
import kotlin.math.abs

/**
 * 屏幕旋转监听
 */
internal class MXSensorHelp private constructor(
    private val context: Context,
    private val minChangeTime: Long = 1500
) {
    private val mHandler = Handler(Looper.getMainLooper())
    private val sensorManager by lazy { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val sensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var isStart = false
    private var _preChangeTime = 0L
    private var _orientation: MXOrientation = MXOrientation.DEGREE_0

    fun getOrientation() = _orientation

    private val listener = ArrayList<MXSensorListener>()
    fun addListener(call: MXSensorListener) {
        if (!listener.contains(call)) {
            listener.add(call)
        }

        if (listener.isNotEmpty() && !isStart) {
            // 有注册监听才开始服务
            start()
        }
    }

    fun deleteListener(call: MXSensorListener) {
        listener.remove(call)
        if (listener.isEmpty() && isStart) {
            stop()
        }
    }

    private fun start() {
        MXUtils.log("MXSensorHelp -> start")
        synchronized(this) {
            if (isStart) return
            sensorManager.unregisterListener(sensorListener)
            sensorManager.registerListener(
                sensorListener,
                sensor,
                SensorManager.SENSOR_DELAY_UI
            )
            isStart = true
        }
    }

    private fun stop() {
        MXUtils.log("MXSensorHelp -> stop")
        synchronized(this) {
            listener.clear()
            sensorManager.unregisterListener(sensorListener)
            isStart = false
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val values = event.values
            val x = values[0]
            val y = values[1]
            if (abs(x) < 3.0 && abs(y) < 3.0) return
            val orientation = if (abs(x) > abs(y)) {
                if (x < 0) MXOrientation.DEGREE_90 else MXOrientation.DEGREE_270
            } else {
                if (y < 0) MXOrientation.DEGREE_180 else MXOrientation.DEGREE_0
            }
            synchronized(this@MXSensorHelp) {
                if (orientation != _orientation) {
                    _orientation = orientation
                    sendOrientationChange(orientation)
                    MXUtils.log("MXSensorHelp -> orientation=$orientation")
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        }
    }

    private fun sendOrientationChange(orientation: MXOrientation) {
        mHandler.removeCallbacksAndMessages(null)
        val sendRun = Runnable {
            listener.toList().forEach { it.onChange(orientation) }
            _preChangeTime = System.currentTimeMillis()
        }

        val delay = minChangeTime - abs(System.currentTimeMillis() - _preChangeTime)
        if (delay > 0) {
            mHandler.postDelayed(sendRun, delay)
        } else {
            mHandler.post(sendRun)
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            MXSensorHelp(MXUtils.applicationContext)
        }
    }
}