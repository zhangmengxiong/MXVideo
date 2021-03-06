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
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 屏幕旋转监听
 */
internal class MXSensorHelp private constructor(
    private val context: Context,
    private val minChangeTime: Long = 1500
) {
    private val DATA_X = 0
    private val DATA_Y = 1
    private val DATA_Z = 2

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
                SensorManager.SENSOR_DELAY_NORMAL
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
            val values: FloatArray = event.values
            var dg = 0
            val x = -values[DATA_X]
            val y = -values[DATA_Y]
            val z = -values[DATA_Z]
            val magnitude = x * x + y * y
            if (magnitude * 4 >= z * z) {
                val oneEightyOverPi = 57.29577957855f
                val angle = (atan2(-y.toDouble(), x.toDouble()) * oneEightyOverPi).toFloat()
                dg = 90 - angle.roundToInt()
                // normalize to 0 - 359 range
                while (dg >= 360) {
                    dg -= 360
                }
                while (dg < 0) {
                    dg += 360
                }
            }
            val orientation = when (dg) {
                in ((90 - 45) until (90 + 45)) -> {
                    MXOrientation.DEGREE_90
                }
                in ((180 - 45) until (180 + 45)) -> {
                    MXOrientation.DEGREE_180
                }
                in ((270 - 45) until (270 + 45)) -> {
                    MXOrientation.DEGREE_270
                }
                else -> {
                    MXOrientation.DEGREE_0
                }
            }
            synchronized(this@MXSensorHelp) {
                if (orientation != _orientation) {
                    _orientation = orientation
                    sendOrientationChange(orientation)
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